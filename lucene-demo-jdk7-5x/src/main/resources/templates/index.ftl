<!doctype html>
<html lang="zn-ch">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">



    <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- 可选的 Bootstrap 主题文件（一般不用引入） -->
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
    <link rel="stylesheet" href="/lucene-demo/css/bootstrap-datetimepicker.min.css" >
    <style>
        .highlighter {
		    font-weight: bold;
		    color: blue;
		}
    </style>
    <title>Lucene搜索样例程序</title>
</head>
<body>
<h1>Lucene搜索样例</h1>
<div class="container-fluid">
    <div class="panel panel-default">
        <div class="panel-heading">测试搜索</div>
        <div class="panel-body">
            <form>
                <div class="form-group">
                    <label for="exampleInputEmail1">内容搜索</label>
                    <input type="text" class="form-control" id="content" placeholder="短信内容">
                </div>
                <div class="form-group">
                    <label for="exampleInputEmail1">手机号精确查找</label>
                    <input type="text" class="form-control" id="mobile" placeholder="手机号">
                </div>
                <div class="form-group">
                    <label for="exampleInputEmail1">SendDate范围搜索</label>
                    <input type="text" id="sendDateBegin">
                    <span>-</span>
                    <input type="text" id="sendDateEnd">
                </div>
                <div class="form-group">
                    <label for="pageSize">每页显示</label>
                    <input type="text" class="form-control" id="pageSize" placeholder="每页显示" value="10">
                    <label for="start">页数</label>
                    <input type="text" class="form-control" id="start" placeholder="页数" value="1">
                </div>
                <button type="button" id="submit" class="btn btn-default">搜索</button>
            </form>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">测试添加索引</div>
        <div class="panel-body">
            <form>
                <div class="form-group">
                    <label for="exampleInputEmail1">短信内容</label>
                    <input type="text" class="form-control" id="content_create" placeholder="短信内容">
                </div>
                <div class="form-group">
                    <label for="exampleInputEmail1">手机号</label>
                    <input type="text" class="form-control" id="mobile_create" placeholder="手机号">
                </div>
                <div class="form-group">
                    <label for="exampleInputEmail1">SendDate</label>
                    <input type="text" id="sendDate_create">
                </div>
                <button type="button" id="add-to-index" class="btn btn-default">添加到索引</button>
            </form>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">测试移除索引</div>
        <div class="panel-body">
            <form>
                <div class="form-group">
                    <label for="exampleInputEmail1">ID</label>
                    <input type="text" class="form-control" id="doc_id" placeholder="ID">
                </div>

                <button type="button" id="delete" class="btn btn-default">这个ID我不要了</button>
            </form>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">结果</div>
        <div class="panel-body">
            <pre id="searchRs"></pre>
        </div>
    </div>
</div>



<script src="https://code.jquery.com/jquery-3.3.1.min.js"
        integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
        crossorigin="anonymous"></script>

<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
<script src="/lucene-demo/js/bootstrap-datetimepicker.min.js"></script>
<script src="/lucene-demo/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<script type="text/javascript">
    $('#sendDateBegin').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        language: 'zh-CN',
        autoclose: true
    });
    $('#sendDateEnd').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        language: 'zh-CN',
        autoclose: true
    });
    $('#sendDate_create').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        language: 'zh-CN',
        autoclose: true
    });
    $("#submit").on("click", function () {
        $.ajax({
            url:"searcher",
            type:"GET",
            data:{
                pageSize: $('#pageSize').val(),
                start:$('#start').val() - 1,
                sendDateBegin: $('#sendDateBegin').val(),
                sendDateEnd: $('#sendDateEnd').val(),
                mobile: $('#mobile').val(),
                contentKeyword: $('#content').val()
            },
            success: function(json) {
                if(json.success) {
                    $("#searchRs")[0].innerHTML = JSON.stringify(json.data, undefined, 2);
                }
            }
        });
    });

    $("#add-to-index").on("click", function () {
        $.ajax({
            url:"doc",
            type:"POST",
            data:{
                sendDate: $('#sendDate_create').val(),
                mobile: $('#mobile_create').val(),
                content: $('#content_create').val()
            },
            success: function(json) {
                $("#searchRs")[0].innerHTML = JSON.stringify(json, undefined, 2);
            }
        });
    });

    $("#delete").on("click", function () {
        $.ajax({
            url:"doc/"+$('#doc_id').val(),
            type:"DELETE",
            success: function(json) {
                $("#searchRs")[0].innerHTML = JSON.stringify(json, undefined, 2);
            }
        });
    });
</script>
</body>
</html>