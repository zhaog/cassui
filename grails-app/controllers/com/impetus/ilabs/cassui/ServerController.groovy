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


class ServerController {

	def cassService
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
		def serverInfo=cassService.getServerInfo(server.getServer(),server.getPort(),server.getAdminLogin(),server.getAdminPassword())
		def keyspace = serverInfo.keyspaces[params.keyspace]
		params.colDetails = serverInfo.keyspaces.get(params.keyspace).get(params.colFamily)
		params.keySlices=[:]
		params.keyCount = cassService.getDataInfo(server.getServer(),server.getPort(),server.getAdminLogin(),server.getAdminPassword(),params.keyspace,params.colFamily,params.max)?.keySlices?.size()
		render(view: "keyspace", model: [params:params,serverInstanceTotal:0])
	}

	def keyspace = {
		if (!params.id){
			redirect(action: "list", params: params)
			return
		}
		def server = Server.get(params.id)
		def serverInfo=cassService.getServerInfo(server.getServer(),server.getPort(),server.getAdminLogin(),server.getAdminPassword())
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
			params.keySlices = cassService.getDataInfo(server.getServer(),server.getPort(),server.getAdminLogin(),server.getAdminPassword(),params.keyspace,params.colFamily,params.max,params.key,params.key)
       	} else {
			params.keySlices = cassService.getDataInfo(server.getServer(),server.getPort(),server.getAdminLogin(),server.getAdminPassword(),params.keyspace,params.colFamily,params.max)
        
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
		//println serverInstance.cassandraURL
        def serverInfo
		if (params.keyspace){
			serverInfo = cassService.getServerInfo(serverInstance.getServer(),serverInstance.getPort(),serverInstance.getAdminLogin(),serverInstance.getAdminPassword(),params.keyspace)
		} else {
			serverInfo = cassService.getServerInfo(serverInstance.getServer(),serverInstance.getPort(),serverInstance.getAdminLogin(),serverInstance.getAdminPassword())
		}
        
		if (!serverInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'server.label', default: 'Server'), params.id])}"
            redirect(action: "list")
        }
        else {
			if (!serverInfo) {
				flash.message = "Please check if server is running!"
			} else if (serverInfo.exception) {
				flash.message = "Error! " + serverInfo.exception
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
            }catch (org.springframework.dao.DataIntegrityViolationException e) {
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
