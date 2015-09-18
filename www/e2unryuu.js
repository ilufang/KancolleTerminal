var e2working;

function e2unryuu () {
	e2working = true;
	document.querySelector("#e2udata").innerHTML="Started.<br />\n";
	tmpl.page="e2u";
	e2sortie(true);
}

function e2sortie(working) {
	if (!working) {
		document.querySelector("#e2udata").innerHTML="KCID Required.";
		return;
	};
	$.ajax({
		url: "api/e2u",
		method: "POST",
		data: {
			action: "sortie"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				document.querySelector("#e2udata").innerHTML += data.loot+"<br />\n";
				port(e2fixes);
			} else {
				document.querySelector("#e2udata").innerHTML += data.reason+"<br />\n";
			}
		} 
	});
}

function e2fixes(working) {
	if (!working) {
		document.querySelector("#e2udata").innerHTML="KCID Required.";
		return;
	};
	console.log("e2fixes");
	$.ajax({
		url: "api/e2u",
		method: "POST",
		data: {
			action: "fixes"
		},
		dataType: "json",
		failure: ajaxError,
		success: function(data) {
			if (data.status=="success") {
				document.querySelector("#e2udata").innerHTML += "Wait:"+(data.wait/1000/60)+"mins<br />\n";
				console.log("Wait:"+(data.wait/1000)+"s");
				if (e2working) {
					setTimeout(function() {
						port(e2sortie);
					},data.wait);
				}
			} else {
				document.querySelector("#e2udata").innerHTML += data.reason+"<br />\n";
			}
		}
	});
}