// JavaScript Document
var banner = document.getElementsByClassName("banner");
var items = document.getElementsByClassName("item"); //图片
var points = document.getElementsByClassName("point") //点
var left = document.getElementById("left"); //左按钮
var right = document.getElementById("right"); //右按钮

var index = 0; //记录在播放哪张图
var num = 0; //重新定义定时器时间 

var clearActive = function() { //重置样式
    for (var i = 0; i < items.length; i++) { //重置所有图片class
        items[i].className = "item";
    }
    for (var i = 0; i < points.length; i++) { //重置所有点class
        points[i].className = "point"
    }
}
var goindex = function() { //图片跳转
    clearActive() //每次跳转都重置所有样式
    items[index].className = "item active"; //把下标为index的图片class改为item active让其显示
    points[index].className = "point active"; //把下标为index的点class改为point active让其突出显示
}

var goLeft = function() { //左按钮函数
    if (index > 0) { //当index大于0时就减1
        index--;

    } else {
        index = 4; //小于0就直接跳到最后一张
    }
    goindex(); //跳转
}

left.onclick = function() { //点击左按钮
    goLeft(); //调用左按钮函数 
    num = 0; //重置定时器时间
}
var goRight = function() { //右按钮函数
    if (index < 4) { //index小于4就加1
        index++;
    } else {
        index = 0; //大于4就直接跳到第1张图片
    }
    goindex() //跳转
}

right.onclick = function() { //点击右按钮
    goRight(); //调用右按钮函数
    num = 0; //重置定时器时间
}
for (var i = 0; i < points.length; i++) { //点击小点跳转
    points[i].onclick = function() { //遍历所有的点添加点击事件
        var pointIndex = this.getAttribute("data-index"); //点击哪个点就返回哪个数（在HTML里定义值）用pointIndex变量接收
        index = pointIndex; //将pointIndex赋值给index
        goindex() //跳转
        num = 0; //重置定时器时间
    }
}
colse = setInterval(function() { //轮播图定时器
    num++; //每次执行定时器num就加1
    if (num == 4) { //当num=40时(表示过了4s)
        goRight() //跳转
        num = 0; //重置定时器时间
    }

}, 1000) //每1s执行一次定时器
banner[0].onmouseover = function() { //鼠标移入停止定时器
    clearInterval(colse);
}
banner[0].onmouseout = function() { //鼠标移出启动定时器
    colse = setInterval(function() {
        num++;
        if (num == 4) {
            goRight()
            num = 0;
        }

    }, 1000)
}