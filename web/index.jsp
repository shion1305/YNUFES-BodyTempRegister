<%@ page import="java.util.ArrayList" %>
<%@ page import="com.shion1305.ynufes.bodytemp2022.contoller.ProcessorManager" %><%--
  Created by IntelliJ IDEA.
  User: shion
  Date: 2022/05/29
  Time: 17:56
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <link href="index.css" type="text/css" rel="stylesheet">
  <title>Title</title>
</head>
<body>
<header>
  <h1>Lineボット検温記録システム メンテナンス用ページ</h1>
</header>
<div id="main-frame">
  <div>
    <h2>現在の状況</h2>
    <table id="status-table">
      <tr>
        <th>設定名</th>
        <th>稼働状況</th>
        <th>Line Quota使用量</th>
      </tr>
      <% ProcessorManager.StatusDataGroup dataGroup = ProcessorManager.StatusDataManager.getStatusData();
        for (ProcessorManager.StatusData p : dataGroup.data) {%>
      <tr>
        <td>
          <%=p.processName%>
        </td>
        <td>
          <%=p.enabled ? "稼働中" : "停止"%>
        </td>
        <td>

        </td>
      </tr>
      <% }%>
    </table>
  </div>
  <form action="${pageContext.request.contextPath}/maintenance/reload" method="post">
    <p>パスワードを入力してリロード</p>
    <input type="password" name="pass">
    <input type="submit">
  </form>
</div>
</body>
</html>