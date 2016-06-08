<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Application System&nbsp;-&nbsp;homepage</title>
</head>
<body>
<%=request.getParameter("username")%>,Welcome!
<a href="<%=request.getContextPath()%>/logout">Logout</a>
</body>
</html>