var terminate = false;

function battle15() {
	tmpl.status="&nsbp;";
	tmpl.progress_15=0;
	terminate = false;
	document.querySelector("#battle15_quit").style.display="none";
	document.querySelector("#battle15_btns").style.display="none";
	tmpl.page="battle15";
	tmpl.status="战斗开始...";
	$.ajax({
		url: "api/battle15",
		method: "POST",
		data: {
			deck: tmpl.activeDeck,
			action: "begin",
			map_major: tmpl.major_map,
			map_minor: tmpl.minor_map,
			hp_threshold: tmpl.hp_threshold,
			cond_threshold: tmpl.cond_threshold
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				battle15Battle(0);
			} else {
				tmpl.status = "API错误:"+data.reason;
				tmpl.errmsg = "API错误:"+data.reason;
				document.querySelector("#toast_general_error").show();
				battle15Pause();
			}
		}
	});
}

function battle15Battle(count) {
	tmpl.progress_15 = 5+30*count;
	if (count==3) {
		return battle15Pause();
	}
	tmpl.status="战斗中...";
	$.ajax({
		url: "api/battle15",
		method: "POST",
		data: {
			action: "battle"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				document.querySelector("#result15").innerHTML=data.result;
				battle15Battle(count+1);
			} else {
				tmpl.status = "战斗终止.API错误:"+data.reason;
				tmpl.errmsg = "API错误:"+data.reason;
				document.querySelector("#toast_general_error").show();
				// End Now!
				document.querySelector("#battle15_quit").style.display="inline-block";
			}
		}
	})
}

function battle15Pause() {
	port(battle15Continue);
}

function battle15ManualPause() {
	tmpl.status = "请在Flash中切换至任意母港以外页面,不进行任何操作返回母港以刷新出击状态"
	document.querySelector("#battle15_btns").style.display="block";
}

function battle15Continue(working) {
	if (!working) {
		battle15ManualPause();
		return;
	};
	document.querySelector("#battle15_btns").style.display="none";
	tmpl.status = "补给...";
	$.ajax({
		url: "api/battle15",
		method: "POST",
		data: {
			action: "resupply"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				if (terminate) {
					tmpl.progress_15 = 100;
					tmpl.status = "战斗结束.";
					document.querySelector("#battle15_quit").style.display="inline-block";
				} else {
					battle15Battle(0);
				}
			} else {
				tmpl.status = "API错误:"+data.reason;
				document.querySelector("#battle15_quit").style.display="inline-block";
			}
		}
	});
}