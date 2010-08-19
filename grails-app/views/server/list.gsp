
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
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="cassandraURL" title="${message(code: 'server.cassandraURL.label', default: 'Cassandra URL')}" />
                            <g:sortableColumn property="cassandraVersion" title="${message(code: 'server.cassandraVersion.label', default: 'Cassandra Version')}" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${serverInstanceList}" status="i" var="serverInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${serverInstance.id}">${fieldValue(bean: serverInstance, field: "cassandraURL")}</g:link></td>
                            <td>${fieldValue(bean: serverInstance, field: "cassandraVersion")}</td>
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
