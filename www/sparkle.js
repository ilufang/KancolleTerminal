
function sparkle(deck, ship, map) {
	// reset variables
	tmpl.sparkle_progress = 5;
	tmpl.objShip = "";
	tmpl.auxShip = "";
	tmpl.sparkleResult = "";
	document.querySelector("#sparkle_window").open();
	document.querySelector("#btn_close_sparkle").style.display="none";
	tmpl.status = "开始刷闪,备份当前编成...";
	$.ajax({
		url: "api/sparkle",
		method: "POST",
		data: {
			action:"begin",
			deckno: deck,
			shipno: ship,
			maparea: map
		},
		dataType: "json",
		failure: ajaxError,
		success: function (data) {
			if (data.status=="success") {
				tmpl.objShip = data.ship;
				tmpl.sparkle_progress=10;
				if (map==1) {
					spAddAuxShip();
				} else {
					spBattle(1);
				}
			} else {
				tmpl.status = "API错误:"+data.reason;
				document.querySelector("#spPauseBtn").style.display="block";
			}
		}
	});
}

function spAddAuxShip() {
	tmpl.status = "添加辅助陪练船只...";
	$.ajax({
		url: "api/sparkle",
		method: "POST",
		data: {
			action: "auxship"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				tmpl.sparkle_progress=20;
				tmpl.auxShip = data.auxships;
				spBattle(1);
			} else {
				tmpl.status = "API错误:"+data.reason;
				document.querySelector("#spPauseBtn").style.display="block";
			}
		}
	});
}

function spBattle(count) {
	tmpl.status = "战斗..."
	if (count==4) {
		return spPause();
	}
	$.ajax({
		url: "api/sparkle",
		method: "POST",
		data: {
			action: "battle"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				tmpl.sparkle_progress = 20+20*count;
				tmpl.sparkleResult += data.result;
				spBattle(count+1);
			} else {
				tmpl.status = "API错误:"+data.reason;
				document.querySelector("#spPauseBtn").style.display="block";
			}
		}
	});
}

function spPause() {
	port(spSupply);
}

function spManualPause() {
	tmpl.status = "请在Flash中切换至任意母港以外页面,不进行任何操作返回母港以刷新出击状态"
	document.querySelector("#spPauseBtn").style.display="block";
}

function spSupply(working) {
	if (!working) {
		spManualPause();
		return;
	};
	tmpl.status = "补给...";
	document.querySelector("#spPauseBtn").style.display="none";
	$.ajax({
		url: "api/sparkle",
		method: "POST",
		data: {
			action: "resupply"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				tmpl.sparkle_progress = 85;
				spRestoreOrg();
			} else {
				tmpl.status = "API错误:"+data.reason;
				tmpl.errmsg = "API错误:"+data.reason;
				document.querySelector("#toast_general_error").show();
				spPause();
			}
		}
	});
}

function spRestoreOrg() {
	tmpl.status = "恢复原编成...";
	$.ajax({
		url: "api/sparkle",
		method: "POST",
		data: {
			action: "restoreOrg"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				tmpl.sparkle_progress = 100;
				tmpl.status = "刷闪完成!3秒后自动关闭";
				loadFleetOrg();
				document.querySelector("#btn_close_sparkle").style.display="inline-block";
				setTimeout(function() {
					document.querySelector("#sparkle_window").close();
				},3000);
			} else {
				tmpl.status = "API错误:"+data.reason;
				tmpl.errmsg = "API错误:"+data.reason;
				document.querySelector("#toast_general_error").show();
				spPause();
			}
		}
	});
}



