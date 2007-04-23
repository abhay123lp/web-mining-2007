<%@ page import="edu.indiana.cs.webmining.blogmining.web.FrontEndHelper" %>
<%@ page import="edu.indiana.cs.webmining.blogmining.web.dto.BlogSearchResult" %>
<%@ page import="java.net.URLDecoder" %>
<%--
  User: Eran Chinthaka (echintha@cs.indiana.edu)
  Date: Mar 23, 2007
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>:: Similar Blog Prediction ::</title></head>
<body>
<h2>Results</h2>

<%
    // first get the two urls from the request
    String firstURL = request.getParameter("firstURL");
//    String secondURL = request.getParameter("secondURL");


    FrontEndHelper frontEndHelper = new FrontEndHelper();
    // check whether they are actually referring to blogs
    if (firstURL == null || "".equals(firstURL) ||
            !frontEndHelper.isBlog(URLDecoder.decode(firstURL))) {
        // very bad, user has not provided us with a proper link, I'm gonna complain
%>
<h3>First URL you provided (<%=firstURL%>) is not linking to a blog.</h3>
<%--<%--%>
<%--} else if (secondURL != null && !"".equals(secondURL) && !frontEndHelper.isBlog(URLDecoder.decode(secondURL))) {--%>
<%--%>--%>
<%--<h3>Second URL you provided (<%=secondURL%>) is not linking to a blog.</h3>--%>
<%
} else {
    // if yes, get the relevant results

    BlogSearchResult[] results = frontEndHelper.getRelevantBlogs(URLDecoder.decode(firstURL));

    // display it to the user
    if (results.length == 0) {
%>
<h3>No similar blogs for the given urls. Please try again with different urls</h3>
<%
} else {
%>
<table cellspacing="1" cellpadding="3" border="0" width="60%">
    <tr bgcolor="#4682B4">
        <th align="center" width="50%"><font color="#ffffff">Blog URL</font></th>
        <th align="center" width="10%"><font color="#ffffff">Score</font></th>
    </tr>
    <%
        for (BlogSearchResult result : results) {
            String url = result.getUrl();
            if (!firstURL.equals(url)) {
    %>
    <tr bgcolor="#dbeaf5" align="left">
        <td width="50%"><font color="#ffffff"><a href="<%=url%>"><%=url%>
        </a> </font></td>
        <td width="10%"><font color="#ffffff"><a
                href="<%=result.getScore()%>"><%=result.getScore()%>
        </a> </font></td>
    </tr>
    <%
            }
        }
    %>
</table>
<%
        }


    }

%>
</body>
</html>