/*
 * Copyright 2010 Impetus Infotech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.impetus.ilabs.cassui

import org.apache.thrift.transport.TSocket;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.thrift.protocol.TBinaryProtocol;

import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeySlice;

class ServerController {

	
	def getServerInfo(server,int port,def exactKeyspace=null){
		def serverInfo = [:]
		TSocket socket = new TSocket(server, port);
		TBinaryProtocol binaryProtocol = new TBinaryProtocol(socket, false, false);
        Cassandra.Client cassandraClient = new Cassandra.Client(binaryProtocol);
		try {
            socket.open();
			serverInfo["clusterName"] = cassandraClient.get_string_property("cluster name")
			def keyspaces = [:]
			for (String keyspace : cassandraClient.describe_keyspaces()) {
                // Ignore system column family
                if (keyspace.equals("system"))
                   continue;
				if (exactKeyspace && keyspace.equals(exactKeyspace)) {
					keyspaces = [:]
					keyspaces.put(keyspace,cassandraClient.describe_keyspace(keyspace)?.sort{ a, b -> a?.key <=> b?.key })
					break;
				} else {
					keyspaces.put(keyspace,cassandraClient.describe_keyspace(keyspace)?.sort{ a, b -> a?.key <=> b?.key })
				}
            }
			 keyspaces= keyspaces.sort { a, b -> a.key.toUpperCase() <=> b.key.toUpperCase() }
			//println keyspaces
			serverInfo["keyspaces"]= keyspaces 
			socket.close();
			socket=null;
		}
        catch (Exception e) {
            e.printStackTrace();
        }
		return serverInfo
	}
	def getDataInfo(server,int port,keyspace,columnFam,maxCount){
		return getDataInfo(server,port,keyspace,columnFam,maxCount,"", "")
	}

	def getDataInfo(server,int port,keyspace,columnFam,maxCount,startKey, endKey){
		def serverInfo = [:]
		TSocket socket = new TSocket(server, port);
		TBinaryProtocol binaryProtocol = new TBinaryProtocol(socket, false, false);
        Cassandra.Client cassandraClient = new Cassandra.Client(binaryProtocol);
		try {
            socket.open();
			KeyRange keyRange = new KeyRange(maxCount);
			keyRange.setStart_key(startKey);
			keyRange.setEnd_key(endKey);

			SliceRange sliceRange = new SliceRange();
			
			sliceRange.setStart(new byte[0] );
			sliceRange.setFinish(new byte[0]);

			SlicePredicate slicePredicate = new SlicePredicate();
			slicePredicate.setSlice_range(sliceRange);

			List keySlices = cassandraClient.get_range_slices(keyspace,
			new ColumnParent(columnFam), slicePredicate, keyRange, ConsistencyLevel.ONE);

			println "count="+keySlices.size()
			serverInfo.keySlices = keySlices
			socket.close();
			socket=null;
		}
        catch (Exception e) {
            e.printStackTrace();
        }
		return serverInfo
	}
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

	def countOnlyKeyspace= {
		if (!params.id){
			redirect(action: "list", params: params)
			return
		}
		def server = Server.get(params.id)
		def serverInfo=getServerInfo(server.getServer(),server.getPort())
		def keyspace = serverInfo.keyspaces[params.keyspace]
		params.colDetails = serverInfo.keyspaces.get(params.keyspace).get(params.colFamily)
		params.keySlices=[:]
		params.keyCount = getDataInfo(server.getServer(),server.getPort(),params.keyspace,params.colFamily,params.max)?.keySlices?.size()
		render(view: "keyspace", model: [params:params,serverInstanceTotal:0])
	}

	def keyspace = {
		if (!params.id){
			redirect(action: "list", params: params)
			return
		}
		def server = Server.get(params.id)
		def serverInfo=getServerInfo(server.getServer(),server.getPort())
		def tables=[]
		serverInfo.keyspaces[params.keyspace]?.each{keyspacestemp->
			tables.add(keyspacestemp.key)
			
		}
		params.tables=tables

		def keyspace = serverInfo.keyspaces[params.keyspace]
		params.colDetails = serverInfo.keyspaces.get(params.keyspace).get(params.colFamily)
		if (params.CountSubmit){
			params.max=5000000
		} else {
			if (params.max=="Are you nuts!") {
				params.max =  1000000
			}
			params.max = Math.min(params.max ? params.int('max') : 10, 1000000)
		}
		if (params.key){
			params.keySlices = getDataInfo(server.getServer(),server.getPort(),params.keyspace,params.colFamily,params.max,params.key,params.key)
       	} else {
			params.keySlices = getDataInfo(server.getServer(),server.getPort(),params.keyspace,params.colFamily,params.max)
        
		}
		params.keyCount = params.keySlices?.keySlices?.size()
		if (params.CountSubmit){
			params.keySlices=[:]
		}
		[params:params,serverInstanceList: Server.list(params), serverInstanceTotal: Server.count()]
    }

	
    def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [serverInstanceList: Server.list(params), serverInstanceTotal: Server.count()]
    }

    def create = {
        def serverInstance = new Server()
        serverInstance.properties = params
        return [serverInstance: serverInstance]
    }

    def save = {
        def serverInstance = new Server(params)
        if (serverInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'server.label', default: 'Server'), serverInstance.id])}"
            redirect(action: "show", id: serverInstance.id)
        }
        else {
            render(view: "create", model: [serverInstance: serverInstance])
        }
    }

    def show = {
        def serverInstance = Server.get(params.id)
		println serverInstance.cassandraURL
        def serverInfo
		if (params.keyspace){
			serverInfo = getServerInfo(serverInstance.getServer(),serverInstance.getPort(),params.keyspace)
		} else {
			serverInfo = getServerInfo(serverInstance.getServer(),serverInstance.getPort())
		}
        
		if (!serverInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
            redirect(action: "list")
        }
        else {
			if (!serverInfo) {
				flash.message = "Please check if server is running!"
			}
            [serverInfo:serverInfo, serverInstance: serverInstance]
        }
    }

    def edit = {
        def serverInstance = Server.get(params.id)
        if (!serverInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [serverInstance: serverInstance]
        }
    }

    def update = {
        def serverInstance = Server.get(params.id)
        if (serverInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (serverInstance.version > version) {
                    
                    serverInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'server.label', default: 'Server')] as Object[], "Another user has updated this Server while you were editing")
                    render(view: "edit", model: [serverInstance: serverInstance])
                    return
                }
            }
            serverInstance.properties = params
            if (!serverInstance.hasErrors() && serverInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'server.label', default: 'Server'), serverInstance.id])}"
                redirect(action: "show", id: serverInstance.id)
            }
            else {
                render(view: "edit", model: [serverInstance: serverInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def serverInstance = Server.get(params.id)
        if (serverInstance) {
            try {
                serverInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
            redirect(action: "list")
        }
    }
}
