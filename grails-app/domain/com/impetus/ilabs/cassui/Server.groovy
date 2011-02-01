package com.impetus.ilabs.cassui

class Server {
	
	String cassandraURL
	String cassandraVersion = "0.6.4"
	String adminLogin
	String adminPassword

	def getServer(){
		if (cassandraURL) {
			if (cassandraURL.indexOf(":")!=-1){
				return cassandraURL.substring(0,cassandraURL.indexOf(":"))
			} else {
				return cassandraURL
			}
		} 
		return "localhost"
	}
	def getPort(){
		if (cassandraURL) {
			if (cassandraURL.indexOf(":")!=-1){
				int port
				try{
					port = Integer.parseInt(cassandraURL.substring(cassandraURL.indexOf(":")+1))
				} catch (Exception e){
					port=9160
				}
				return port
			}
		}
		return 9160
	}

    static constraints = {
		cassandraURL()
		cassandraVersion(inList:["0.6.4","0.7"])
		adminLogin(nullable:true)
		adminPassword(nullable:true)
    }
}
