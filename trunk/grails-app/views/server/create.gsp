

<%@ page import="com.impetus.ilabs.cassui.Server" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'server.label', default: 'Server')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${serverInstance}">
            <div class="errors">
                <g:renderErrors bean="${serverInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="cassandraURL"><g:message code="server.cassandraURL.label" default="Cassandra URL" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'cassandraURL', 'errors')}">
                                    <g:textField name="cassandraURL" value="${serverInstance?.cassandraURL}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="cassandraVersion"><g:message code="server.cassandraVersion.label" default="Cassandra Version" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'cassandraVersion', 'errors')}">
                                    <g:select name="cassandraVersion" from="${serverInstance.constraints.cassandraVersion.inList}" value="${serverInstance?.cassandraVersion}" valueMessagePrefix="server.cassandraVersion"  />
                                </td>
                            </tr>
							<tr class="prop">
                                <td valign="top" class="name">
                                    <label for="adminLogin"><g:message code="server.adminLogin.label" default="Admin Login" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'adminLogin', 'errors')}">
                                    <g:textField name="adminLogin" value="${serverInstance?.adminLogin}" />
                                </td>
                            </tr>                        
							<tr class="prop">
                                <td valign="top" class="name">
                                    <label for="adminPassword"><g:message code="server.adminPassword.label" default="Admin Password" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'adminPassword', 'errors')}">
                                    <g:passwordField name="adminPassword" value="${serverInstance?.adminPassword}" />
                                </td>
                            </tr>     
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="port"><g:message code="server.port.label" default="Port" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'port', 'errors')}">
                                    
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="server"><g:message code="server.server.label" default="Server" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: serverInstance, field: 'server', 'errors')}">
                                    
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
