package com.impetus.ilabs.cassui

import org.apache.thrift.transport.TSocket;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.AuthenticationRequest;
import org.apache.cassandra.thrift.AuthenticationException;


class CassService {
 
    boolean transactional = false
	protected static final String ENCODING = "utf-8";

	
	def private getClient(server,int port,login,password) throws Exception{
		TTransport socket = new TFramedTransport(new TSocket(
				server, port));
		TProtocol framedProtocol = new TBinaryProtocol(socket);
		Cassandra.Client cassandraClient = new Cassandra.Client(framedProtocol);
		cassandraClient.getInputProtocol().getTransport().open();
		if (!login) {
			login=""
		}
		if (!password) {
			password=""
		}
		Map<String, String> credentials = new HashMap();
		credentials.put("username", login);
		credentials.put("password", password);
		AuthenticationRequest ar = new AuthenticationRequest(credentials);
		try {
			cassandraClient.login(ar); 
			//println "authenticated with credentials "+login+"/"+password
		} catch(Exception e) {
			/**
			println "auth failed with plain password- trying with MD5 encryption"
			credentials = new HashMap();
			credentials.put("username", login);
			credentials.put("password", password.encodeAsMD5());
			ar = new AuthenticationRequest(credentials);
			cassandraClient.login(ar); 
			*/
			throw e;
		}		
		return cassandraClient;
	}	
	
	
	def getServerInfo(server,int port, login,passwd,def exactKeyspace=null){
		def serverInfo = [:]
		Cassandra.Client cassandraClient
		try {
			cassandraClient = getClient(server,port,login,passwd);
			serverInfo["clusterName"] = cassandraClient.describe_cluster_name();
			def keyspaces = [:]
			for(KsDef keyspaceObj: cassandraClient.describe_keyspaces()) {
			   String keyspace = keyspaceObj.getName();
                // Ignore system column family
                if (keyspace.equals("system"))
                   continue;
               Map<String, Map<String, String>> describesKeyspaces = new LinkedHashMap<String, Map<String, String>>();
				List<CfDef> cfDefs = keyspaceObj.getCf_defs();
				for (CfDef cfDef : cfDefs) {
					Map<String, String> cfDetails = new LinkedHashMap<String, String>();
					cfDetails
							.put("Comparator Type", cfDef.getComparator_type());
					cfDetails.put("Validation Class", cfDef
							.getDefault_validation_class());

					cfDetails.put("Comment", cfDef.getComment());
					cfDetails.put("Column Type", cfDef.getColumn_type());
					describesKeyspaces.put(cfDef.getName(), cfDetails);


				}
				if (exactKeyspace && keyspace.equals(exactKeyspace)) {
					keyspaces = [:]
					keyspaces.put(keyspace,describesKeyspaces?.sort{ a, b -> a?.key <=> b?.key })
					break;
				} else {
					keyspaces.put(keyspace,describesKeyspaces?.sort{ a, b -> a?.key <=> b?.key })
				}
            }
			 keyspaces= keyspaces.sort { a, b -> a.key.toUpperCase() <=> b.key.toUpperCase() }
			//println keyspaces
			serverInfo["keyspaces"]= keyspaces 
			cassandraClient?.getInputProtocol()?.getTransport()?.close();
		}
        catch (AuthenticationException e) {
			serverInfo["exception"]= "Autentication failure :"+ e
         	//e.printStackTrace();
        }
        catch (Exception e) {
			serverInfo["exception"]= "Exception occured :"+ e
			//e.printStackTrace();
        }
		finally {
			cassandraClient?.getInputProtocol()?.getTransport()?.close();
		}
		return serverInfo
	}
	
	def getDataInfo(server,int port,login,passwd,keyspace,columnFam,maxCount){
		return getDataInfo(server,port,login,passwd,keyspace,columnFam,maxCount,"", "")
	}

	def getDataInfo(server,int port,login,passwd,keyspace,columnFam,maxCount,startKey, endKey){
		def serverInfo = [:]
		Cassandra.Client cassandraClient
		try {
			cassandraClient = getClient(server,port,login,passwd);
			KeyRange keyRange = new KeyRange(maxCount);
			keyRange.setStart_key(startKey.getBytes());
			keyRange.setEnd_key(endKey.getBytes());
			SliceRange sliceRange = new SliceRange();
			sliceRange.setStart(new byte[0]);
			sliceRange.setFinish(new byte[0]);

			SlicePredicate slicePredicate = new SlicePredicate();
			slicePredicate.setSlice_range(sliceRange);
            cassandraClient.set_keyspace(keyspace);
			List keySlices = cassandraClient.get_range_slices(new ColumnParent(
					columnFam), slicePredicate, keyRange,
					ConsistencyLevel.ONE);
					

			//println "count="+keySlices.size()
			serverInfo.keySlices = keySlices
			cassandraClient?.getInputProtocol()?.getTransport()?.close();
		}
        catch (AuthenticationException e) {
			serverInfo["exception"]= "Autentication failure :"+ e
         	//e.printStackTrace();
        }
        catch (Exception e) {
			serverInfo["exception"]= "Exception occured :"+ e
			//e.printStackTrace();
        } finally {
			cassandraClient?.getInputProtocol()?.getTransport()?.close();
		}
		return serverInfo
	}
	
	/**
	def edit(server,int port,keyspace,key) {
		println cassService.getDataInfo(server,port,keyspace,null,1,params.key,params.key)
	
	}
	
	def update(server,int port,keyspace,colFamily,key,affectedParams) {

		println affectedParams
		affectedParams = affectedParams.sort{it.key}
		def socket=getSocket(server,port);
		def cassandraClient = getCassandaraClient(socket)
		try {
			socket.open();
            long timestamp = System.currentTimeMillis();
            Map<String, Map<String, List<Mutation>>> job = new HashMap<String, Map<String, List<Mutation>>>();
            List<Mutation> mutations = new ArrayList<Mutation>();
			List<Column> subColumns = null;
			def superColumnNameCurrent =null;
			
			affectedParams.each {
				def fullKey = it.key
				int separator = fullKey.indexOf('`')
				def superColumnName = fullKey.substring(0,separator)
				if (superColumnName.startsWith('SCF_')) {
					if (superColumnName!=superColumnNameCurrent) {
						superColumnNameCurrent = superColumnName
						subColumns = new ArrayList<Column>();
						SuperColumn col = new SuperColumn(superColumnName.substring(superColumnName.indexOf('SCF_')+'SCF_'.size()).getBytes(ENCODING), subColumns);
						ColumnOrSuperColumn columnOrSuperColumn = new ColumnOrSuperColumn();
						columnOrSuperColumn.setSuper_column(col);	
						Mutation mutation = new Mutation();
						mutation.setColumn_or_supercolumn(columnOrSuperColumn);
						mutations.add(mutation);
					}
					def columnName = fullKey.substring(separator+1)
					def columnValue = it.value
					println keyspace+" "+key+" "+superColumnName+" "+columnName+" "+columnValue
					Column subColumn = new Column(columnName.getBytes(ENCODING), columnValue.getBytes(ENCODING), timestamp);
					if (!columnName.contains('sub~column')) {
						subColumns.add(subColumn);
					}
				} else if (superColumnName.startsWith('CF_')) {
					def columnName = fullKey.substring(separator+1)
					def columnValue = it.value
					ColumnOrSuperColumn columnOrSuperColumn = new ColumnOrSuperColumn();
					println keyspace+" "+key+" "+superColumnName+" "+columnName+" "+columnValue
					Column col = new Column(columnName.getBytes(ENCODING), columnValue.getBytes(ENCODING), timestamp);
					columnOrSuperColumn.setColumn(col);	
					Mutation mutation = new Mutation();
					mutation.setColumn_or_supercolumn(columnOrSuperColumn);
					mutations.add(mutation);
					//ColumnPath columnPath = new ColumnPath(colFamily);
					//columnPath.setColumn(columnName.getBytes(ENCODING));
					//cassandraClient.insert(keyspace,key,columnPath,
                    //    columnValue.getBytes(ENCODING),timestamp,ConsistencyLevel.ALL);
						 
				}
			}		
            Map<String, List<Mutation>> mutationsForColumnFamily = new HashMap<String, List<Mutation>>();
            mutationsForColumnFamily.put(colFamily, mutations);
            job.put(key, mutationsForColumnFamily);
            cassandraClient.batch_mutate(keyspace, job, ConsistencyLevel.ALL);
			closeSocket(socket)
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

	def remove(server,int port,keyspace,colFamily,key,def superColumnName,def isSuperColumn) {
		def socket=getSocket(server,port);
		def cassandraClient = getCassandaraClient(socket)
		try {
  
			socket.open();
			//remove(String keyspace, String key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
			if (superColumnName) {
				ColumnPath columnPath = new ColumnPath(colFamily);
				if (!isSuperColumn) {
					columnPath.setSuper_column(superColumnName.getBytes());
					columnPath.setSuper_columnIsSet(true);
				} else {
					columnPath.setColumn(superColumnName.getBytes());
					columnPath.setColumnIsSet(true);
				}
				cassandraClient.remove(keyspace,key,columnPath,new Date().time,ConsistencyLevel.ALL)
			} else {
				cassandraClient.remove(keyspace,key,new ColumnPath(colFamily),new Date().time,ConsistencyLevel.ALL)
			}
			println "removed key"
			closeSocket(socket)
		}
        catch (Exception e) {
            e.printStackTrace();
        }
		
	
	}
	
	def getSocket(server,int port){
		return new TSocket(server, port);
	}
	
	def getCassandaraClient(socket){
		TBinaryProtocol binaryProtocol = new TBinaryProtocol(socket, false, false);
        Cassandra.Client cassandraClient = new Cassandra.Client(binaryProtocol);
		return cassandraClient
	}
	
	def closeSocket(socket){
		socket.close();
		socket=null;
	}
	
	
	def getServerInfo(server,int port,def exactKeyspace=null){
		def serverInfo = [:]
		def socket=getSocket(server,port);
		def cassandraClient = getCassandaraClient(socket)
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
			closeSocket(socket)
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
		def socket=getSocket(server,port);
		def cassandraClient = getCassandaraClient(socket)
		try {
            socket.open();
			KeyRange keyRange = new KeyRange(maxCount);
			keyRange.setStart_key(startKey?.trim());
			keyRange.setEnd_key(endKey?.trim());
			SlicePredicate slicePredicate = getSlicePredicate();
			List keySlices = cassandraClient.get_range_slices(keyspace,
				new ColumnParent(columnFam), slicePredicate, keyRange, ConsistencyLevel.ALL);
			serverInfo.keySlices = keySlices
			closeSocket(socket);
		}
        catch (Exception e) {
            e.printStackTrace();
        }
		return serverInfo
	}
	
	private def getSlicePredicate(){
		SliceRange sliceRange = new SliceRange();
		sliceRange.setStart(new byte[0] );
		sliceRange.setFinish(new byte[0]);
		SlicePredicate slicePredicate = new SlicePredicate();
		slicePredicate.setSlice_range(sliceRange);
		return slicePredicate;
	}
	*/
}
