<?xml version="1.0"?>
<%@   taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="form"	uri="http://www.springframework.org/tags/form" 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>Beet Demo</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/hello.css" type="text/css" />
	</head>
	<body>
		<div id="wrapper">
			<div class="header">
				<div class="logo"><a class="logo-text" href="http://beet.sourceforge.net">Beet</a></div><h1>Hello world</h1>
			</div>
			
			<c:if test="${result != null}"><p class="result"><c:out value="${result}"/></p></c:if>
			<form:form method="post" commandName="helloWorldRequest">
				<form:hidden id="recordId" path="data.id"/>
				<table class="data">
					<tr>
						<th>ID<br /></th>
						<th>A Field<br /></th>
						<th>Another Field<br /></th>
					</tr>
					<c:choose>
						<c:when test="${empty records}">
					<tr>
						<td colspan="2">No data to display.<br /></td>
						<td><input type="submit" name="command" value="create"/><br /></td>
					</tr>
						</c:when>
						<c:otherwise>
					<c:forEach items="${records}" var="rec">
					<tr class="record">
						<td>${rec.id}<br /></td>
						<td>${rec.AField}<br /></td>
						<td>${rec.anotherField}<br /></td>
						<td><input type="submit" name="command" value="delete" onclick="document.getElementById('recordId').value='${rec.id}'; return true;"/><br /></td>
					</tr>
					</c:forEach>
					<tr>
						<td>New:</td>
						<td><form:input path="data.AField"/></td>
						<td><form:input path="data.anotherField"/></td>
						<td><input type="submit" name="command" value="update"/></td>
					</tr>
					<tr>
						<td colspan="4"><input type="submit" name="command" value="clear"/></td>
					</tr>
						</c:otherwise>
					</c:choose>
					
				</table>
			</form:form>
		</div>
	</body>
</html>