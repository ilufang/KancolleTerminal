var screenw, screenh;
var error="success";
var scimg;
var state=0;

function setup() {
	var scap_prepare = loadJSON("api/kcidprepare", function(scap_prepare){
		if (scap_prepare.status=="success") {
			screenw = scap_prepare.screenw;
			screenh = scap_prepare.screenh;
		} else {
			error = scap_prepare.reason;
			console.log(scap_prepare);
			return;
		}
		createCanvas(screenw/2, screenh/2);
	});
}

function init() {
	loadJSON("api/kcidprepare", function(scap_prepare){
		if (scap_prepare.status=="success") {
				scimg = loadImage("static/screencap.png",function(){
					state=1;
					document.querySelector("#msg").innerHTML="请在图中框出Flash动画左上角(尽量精准)";
				});
		} else {
			error = scap_prepare.reason;
			console.log(scap_prepare);
			return;
		}
		
	});

}


function draw() {
	noFill();
	background(255);
	if (state>0) {
		image(scimg, -mouseX, -mouseY, screenw, screenh);
	}
	if (state<=2&&state>0) {
		if (state==2) {
			stroke(0,0,255,102);
			strokeWeight(3);
			rect(
				x0*2-mouseX,
				y0*2-mouseY,
				mouseX-(x0*2-mouseX),
				mouseY-(y0*2-mouseY)
				);
		};
		stroke(255,0,0);
		strokeWeight(1);
		line(0, mouseY, screenw, mouseY);
		line(mouseX, 0, mouseX, screenh);
	} else {
		stroke(0,255,0);
		strokeWeight(3);
		rect(
			x0*2-mouseX,
			y0*2-mouseY,
			x1*2-mouseX-(x0*2-mouseX),
			y1*2-mouseY-(y0*2-mouseY)
			);
	}
}

var x0, y0, x1, y1;

function mouseClicked() {
	if (state==1) {
		x0=mouseX;
		y0=mouseY;
		state=2;
		document.querySelector("#msg").innerHTML="请在图中框出Flash动画右下角(尽量精准)";

	} else if (state==2) {
		x1=mouseX;
		y1=mouseY;
		state=3;
		document.querySelector("#msg").innerHTML="请稍等...";
		$.ajax({
			url: "api/kcidinit",
			method: "POST",
			data: {
				x0: Math.round(x0*2),
				y0: Math.round(y0*2),
				x1: Math.round(x1*2),
				y1: Math.round(y1*2)
			},
			failure: function() {
				document.querySelector("#msg").innerHTML="请求错误:Ajax请求失败";
			},
			success: function(data) {
				if (data.status=="success") {
					window.location.assign("remote.html");
					document.querySelector("#msg").innerHTML="欢迎使用KCT/InputDaemon!";
				} else {
					document.querySelector("#msg").innerHTML="请求错误:"+data.reason;
				}
			}
		})
	}
}