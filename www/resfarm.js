var farm_begin;
var rfworking;
var rfmajor,rfminor;

function resFarm() {
	document.querySelector("#resfarm_quit").style.display="none";
	tmpl.status="打捞中...";
	farm_begin = new Date().getTime();
	rfworking=true;
	switch (tmpl.resfarm_map) {
		case "1-2":
			rfmajor=1;
			rfminor=2;
			break;
		case "1-4":
			rfmajor=1;
			rfminor=4;
			break;
		case "2-2":
			rfmajor=2;
			rfminor=2;
			break;
		case "2-3":
			rfmajor=2;
			rfminor=3;
			break;
		default:
			tmpl.errmsg="无效的地图";
			document.querySelector("#toast_general_error").show();
			return;
	}
	tmpl.resfarm_result="";
	tmpl.resfarm_fuel=0;
	tmpl.resfarm_fuel_rate=0;
	tmpl.resfarm_ammo=0;
	tmpl.resfarm_ammo_rate=0;
	tmpl.resfarm_steel=0;
	tmpl.resfarm_steel_rate=0;
	tmpl.resfarm_baux=0;
	tmpl.resfarm_baux_rate=0;
	tmpl.resfarm_count=0;
	tmpl.resfarm_time="";
	resfarmTimer();
	resFarmSortie();
	tmpl.page="resfarm";
}

function resfarmTimer() {
	if (rfworking) {
		setTimeout(resfarmTimer,1000);
	}
	var time_elapsed = new Date().getTime()-farm_begin; // ms
	tmpl.resfarm_fuel_rate=Math.round(tmpl.resfarm_fuel*1000*60*60/time_elapsed);
	tmpl.resfarm_ammo_rate=Math.round(tmpl.resfarm_ammo*1000*60*60/time_elapsed);
	tmpl.resfarm_steel_rate=Math.round(tmpl.resfarm_steel*1000*60*60/time_elapsed);
	tmpl.resfarm_baux_rate=Math.round(tmpl.resfarm_baux*1000*60*60/time_elapsed);
	time_elapsed/=1000; // second
	time_elapsed = Math.round(time_elapsed);
	var rft_second = time_elapsed%60;
	time_elapsed-=rft_second;
	time_elapsed/=60; // minute
	var rft_minute = time_elapsed%60;
	time_elapsed-=rft_minute;
	time_elapsed/=60; // hour
	var rft_hour = time_elapsed;
	var time_str=" ";
	if (rft_hour>0) {
		time_str+=rft_hour+"h ";
	}
	if (rft_minute>0||rft_hour>0) {
		time_str+=rft_minute+"min ";
	}
	time_str+=rft_second+"s";
	tmpl.resfarm_time=time_str;

}

function resFarmSortie() {
	console.log("resfarm sortie.");
	$.ajax({
		url: "api/resfarm",
		method: "POST",
		data: {
			map_major: rfmajor,
			map_minor: rfminor,
			deck: tmpl.activeDeck
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				resFarmGotResult(data.result[0],data.result[1],data.result[2],data.result[3]);
			} else {
				resFarmFinalize();
				tmpl.status="请求错误:"+data.reason;
			}
		}
	});
}

function resFarmFinalize() {
	rfworking=false;
	tmpl.status="已终止.你可以安全地离开这一页面了.";
	document.querySelector("#resfarm_quit").style.display="inline-block";
}

function resFarmGotResult(fuel, ammo, steel, baux) {
	tmpl.resfarm_fuel+=fuel;
	tmpl.resfarm_ammo+=ammo;
	tmpl.resfarm_steel+=steel;
	tmpl.resfarm_baux+=baux;
	tmpl.resfarm_count++;
	tmpl.resfarm_result="#"+tmpl.resfarm_count+": "+fuel+"/"+ammo+"/"+steel+"/"+baux;
	if (fuel+ammo+steel+baux!=0) {
		setTimeout(function(){
			port(resFarmContinue);
		},5000);
	} else {
		resFarmContinue(true);
	}

}

function resFarmContinue(working) {
	if (!working) {
		tmpl.errmsg = "需要按键精灵(KCID)";
		document.querySelector("#toast_general_error").show();
	} else {
		if (rfworking) {
			setTimeout(resFarmSortie,2000);
		} else {
			resFarmFinalize();
		}
	}
}