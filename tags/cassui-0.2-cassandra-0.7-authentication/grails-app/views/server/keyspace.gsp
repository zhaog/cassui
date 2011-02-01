
<%@ page import="com.impetus.ilabs.cassui.Server" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'server.label', default: 'Server')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
		
            <h1>Keyspace - <g:link action="show" id="${params.id}" params="[keyspace:params.keyspace]">${params.keyspace}</g:link></h1>
			<small> <br/>
			
			<g:each var="table" status="i" in="${params.tables}">
			<g:if test="table.toString().contains('_fk')">
				<g:link action="keyspace" id="${params.id}" params="[keyspace:params.keyspace,colFamily:table]"><i>${table}</i> </g:link>
			
			</g:if>
			<g:else>
				<g:link action="keyspace" id="${params.id}" params="[keyspace:params.keyspace,colFamily:table]">${table} </g:link>
				<g:if test="${((i+1) % 12) == 0}">
					<br/>
				</g:if>
			</g:else>			
			
			</g:each>
			</small>
			<h3>Column Family - ${params.colFamily} </h3>
			<h6>
			<table border="1" style="width:1010px">
				<tr>
				<g:each var="coldetail" status="y" in="${params.colDetails?.sort{a,b-> b.key<=> a.key}}">
					<td>${coldetail.key}: ${coldetail.value.replace("org.apache.cassandra.db.marshal.","")}</td>
				</g:each>
				</tr>
			</table>
			</h6>
			<h7>Total count fetched = ${params?.keyCount
}    
			</h7>
			<div class="list">
			<g:form name="myform" action="keyspace" >
				Change fetch size here :
						<g:select onchange="this.form.submit()" name="max" from="${['10','100','1000','10000','100000','1000000','Are you nuts!']}" value="${params.max}"/>
						<g:hiddenField name="keyspace" value="${params.keyspace}"/>
						<g:hiddenField name="colFamily" value="${params.colFamily}"/>
						<g:hiddenField name="id" value="${params.id}"/>
						<br/>
						Key lookup: <g:textField name="key" value="${params.key}"/>
						<g:submitButton name="keySubmit" value="Submit" />
						<input type="button" value="Reset" onClick="resetMe(this);"/>
						<g:submitButton name="CountSubmit" value="Get Count Only" />
						<script>
						<!--
							function resetMe(somevar){
								somevar.form.key.value='';
								document.getElementById('myform').submit();
								
							}
						-->
						</script>
			</g:form>
					<br/><small>(by default only first 10 records are fetched- the total count above shows actual or the fetch size)</small>
			</div>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <tbody>
                    <g:each in="${params?.keySlices?.keySlices}" status="i" var="keySlice">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						
                            <td>${new String(keySlice.getKey())}</td>
							
								<g:each in="${keySlice.getColumns()}" status="k" var="column">
								
										<g:if test="${column.isSetColumn()}">
												<td>
												<table>
													<tr>
														<td>										
														<g:if test="${column?.getColumn()?.value}">
															(CF)-${new String(column?.getColumn().name)}:${new String(column?.getColumn()?.value)}
														</g:if>
														</td>
													</tr>
												</table>				
												</td>
										</g:if>
										<g:else>
												<td>
													<table>
														<tr>
															<td>
															(SCF)- ${new String(column.getSuper_column().name)}
															</td>
															<td>
																<table><tr>
																<g:each in="${column.getSuper_column().columns}" status="l" var="sub_column">
																<td>
																(child)-${new String(sub_column.name)+":"+new String(sub_column.value)}
																</td>
																</g:each>
																</tr></table>
															</td>
															
														</tr>
													</table>
												</td>
										</g:else>
								</g:each>	
					    </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${serverInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
