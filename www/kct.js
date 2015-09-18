function ajaxError () {
	tmpl.errmsg = "无法连接至服务器,请确认Java程序正在运行";
	document.querySelector("#toast_general_error").show();
	tmpl.ajaxLoading = false;
}

function shutdown() {
	$.ajax({
		url: "api/shutdown",
		method: "POST",
		data: {},
		dataType: "json",
		failure: ajaxError,
		success: function() {
			tmpl.errmsg = "服务器已关闭,请退出所有KancolleTerminal页面.";
			document.querySelector("#toast_general_error").show();
		}
	});
}

function develop () {
	tmpl.ajaxLoading = true;
	$.ajax({
		url: "api/develop",
		method: "POST",
		data: {
			fuel: tmpl.dfuel,
			ammo: tmpl.dammo,
			steel: tmpl.dsteel,
			baux: tmpl.dbaux,
			count: tmpl.dcount
		},
		failure: ajaxError,
		success: function(data) {
			tmpl.ajaxLoading = false;
			if (data.status!="success") {
				tmpl.errmsg = "开发请求失败:"+data.reason;
				document.querySelector("#toast_general_error").show();
			} else {
				document.querySelector("#arsenal_result").innerHTML = data.data;
				tmpl.arsenal_page = "result";
				tmpl.dcount = "";
				tmpl.dfuel = "";
				tmpl.dammo = "";
				tmpl.dsteel = "";
				tmpl.dbaux = "";
			}
		}
	});
}

function construct() {
	if (available_slot==-1) {
		tmpl.errmsg = "没有空闲的建造渠.请刷新建造页面.";
		document.querySelector("#toast_general_error").show();
		return;
	}
	tmpl.ajaxLoading = true;
	$.ajax({
		url: "api/construct",
		method: "POST",
		data: {
			fuel: tmpl.cfuel,
			ammo: tmpl.cammo,
			steel: tmpl.csteel,
			baux: tmpl.cbaux,
			seaweed: tmpl.cseaweed,
			lsc: tmpl.clarge,
			fire: tmpl.cfire,
			count: tmpl.ccount,
			slot: available_slot
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			tmpl.ajaxLoading = false;
			if (data.status=="success") {
				if (tmpl.cfire) {
					// Load instant results
					document.querySelector("#arsenal_result").innerHTML = data.data;
					tmpl.arsenal_page = "result";
		
				} else {
					// Switch to construction page
					tmpl.arsenal_page = "buildstatus";
				}
				tmpl.ccount = "";
				tmpl.cfuel = "";
				tmpl.cammo = "";
				tmpl.csteel = "";
				tmpl.cbaux = "";
				tmpl.clarge = false;
				tmpl.cfire = false;
				tmpl.cseaweed = "";
				loadBuildDock();

			} else {
				tmpl.errmsg = "建造请求失败:"+data.reason;
				document.querySelector("#toast_general_error").show();
			}
		}
	})
}

function loadFleetOrg() {
	document.querySelector("#fleet").innerHTML="";
	tmpl.ajaxLoading=true;
	$.ajax({
		url: "api/getfleetorg",
		method: "POST",
		data: {},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			document.querySelector("#sally_deck_selector").innerHTML="";

			tmpl.ajaxLoading = false;
			var fleetDiv = document.querySelector("#fleet");
			var decks = data.data;
			for (var i = 0; i < decks.length; i++) {
				var deckDiv = document.createElement("div");
				deckDiv.appendChild(document.createTextNode(decks[i].name));
				deckDiv.appendChild(document.createElement("hr"));
				for (var j = 0; j < decks[i].ships.length; j++) {
					var shipDiv = document.createElement("div");
					var ship = decks[i].ships[j];
					var infodiv = document.createElement("div");
					infodiv.innerHTML="<h2 style='display:inline'>"+ship.name+"</h2><span>Lv."+ship.lv+" HP:"+ship.hp+"/"+ship.maxhp+" Cond."+ship.condition+"</span>";
					var actiondiv = document.createElement("div");
					actiondiv.innerHTML="\
					<paper-button onclick='sparkle("+(i+1)+","+(j+1)+",1)' class='plain'>刷闪(1-1)</paper-button>\
					<paper-button onclick='sparkle("+(i+1)+","+(j+1)+",5)' class='plain'>刷闪(1-5)</paper-button>\
					";
					shipDiv.appendChild(infodiv);
					shipDiv.appendChild(actiondiv);
					deckDiv.appendChild(shipDiv);
					deckDiv.appendChild(document.createElement("br"));
				}
				fleetDiv.appendChild(deckDiv);
				fleetDiv.appendChild(document.createElement("br"));
				fleetDiv.appendChild(document.createElement("br"));
				// Additional: inflate decks in sally page
				var sallyEntry = document.createElement("paper-item");
				sallyEntry.setAttribute("deckno",i+1);
				sallyEntry.innerHTML=decks[i].name;
				document.querySelector("#sally_deck_selector").appendChild(sallyEntry);

			}
		}
	});
}

var available_slot = -1;

function loadBuildDock() {
	document.querySelector("#bdock").innerHTML="";
	tmpl.ajaxLoading = true;
	$.ajax({
		url: "api/bdock",
		method: "POST",
		data: {},
		failure: ajaxError,
		success: function(data) {
			tmpl.ajaxLoading = false;
			if (data.status!="success") {
				tmpl.errmsg = "获取建造渠失败:"+data.reason;
				document.querySelector("#toast_general_error").show();
			} else {
				available_slot = -1;
				var bdockDiv = document.querySelector("#bdock");
				for (var i=0; i<data.data.length; i++) {
					var dock = data.data[i];
					var dockDiv = document.createElement("div");
					dockDiv.innerHTML = ""+dock.api_id+" ";
					switch(dock.api_state) {
						case -1:
							dockDiv.innerHTML+="**被锁定**";
							break;
						case 0:
							dockDiv.innerHTML+="--空闲中--";
							available_slot=dock.api_id;
							break;
						case 2:
							dockDiv.innerHTML+="==建造中==";
							dockDiv.innerHTML+=" (结束时间:JST-"+dock.api_complete_time_str+")";
							break;
						case 3:
							dockDiv.innerHTML+="==已完成==";
							break;
					}
					bdockDiv.appendChild(dockDiv);
					if (dock.api_created_ship_id!=0) {
						var shipDiv = document.createElement("div");
						shipDiv.innerHTML="<h2 style='display:inline'>"+data.ship_name[i]+"</h2>";
						shipDiv.innerHTML+="<h3 style='display:inline'>"+dock.api_item1+"/"+dock.api_item2+"/"+dock.api_item3+"/"+dock.api_item4+"@"+dock.api_item5+"</h3>";
						bdockDiv.appendChild(shipDiv);
					};
					bdockDiv.appendChild(document.createElement("br"));
					bdockDiv.appendChild(document.createElement("hr"));
					bdockDiv.appendChild(document.createElement("br"));
				}
			}
		}
	})
}

function periodicUpdate(){
	// TODO update resources, etc.
}

function port(callback) {
	$.ajax({
		url: "api/port",
		method: "POST",
		data: {
			action: "request"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				waitPort(callback);
			} else if (data.status=="not_supported") {
				callback(false);
			} else {
				tmpl.errmsg = "回港请求错误:"+data.reason;
			}
		}
	});
}

function waitPort(callback) {
	$.ajax({
		url: "api/port",
		method: "POST",
		data: {
			action: "wait"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.finished) {
				callback(true);
			} else {
				setTimeout(function() {
					waitPort(callback);
				},500);
			}
		}
	});
}

function exportDrops() {
	$.ajax({
		url: "api/droplog",
		method: "POST",
		data: {},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			tmpl.errmsg = "掉落日志导出完成.";
			document.querySelector("#toast_general_error").show();
		}
	})
}

function exportBuilds() {
	$.ajax({
		url: "api/buildlog",
		method: "POST",
		data: {},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			tmpl.errmsg = "建造日志导出完成.";
			document.querySelector("#toast_general_error").show();
		}
	})
}

