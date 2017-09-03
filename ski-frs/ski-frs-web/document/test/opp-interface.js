
(function($) {

fomjar.framework.phase.append('dom', build_body);

function build_body() {
	var input_did;
	frs.ui.body().append([
		new frs.ui.Button('测试 - 文件 - 人脸', function() {
			fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
				opp		: 'opp-1',
				did		: 'device-e64392b71a8945cc8c6803aa28852304',
				'dev-type'	: 0,
				'task-type'	: 0,
				oper	: 1,
				path	: 'D:/mywork/video/exit.avi',
			}, function(code, desc) {
				frs.ui.body().append($('<div></div>').append('code: ' + code + '; desc: ' + desc));
			});
		}).to_major(),
		new frs.ui.Button('测试 - 文件 - 人形', function() {
			fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
				opp		: 'opp-1',
				did		: 'device-e64392b71a8945cc8c6803aa28852304',
				'dev-type'	: 0,
				'task-type'	: 1,
				oper	: 1,
				path	: 'D:/mywork/video/exit.avi',
			}, function(code, desc) {
				frs.ui.body().append($('<div></div>').append('code: ' + code + '; desc: ' + desc));
			});
		}).to_major(),
		$('<br/>'),
		new frs.ui.Button('测试 - 海康 - 人脸', function() {
			fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
				opp		: 'opp-1',
				did		: 'device-e64392b71a8945cc8c6803aa28852304',
				'dev-type'	: 1,
				'task-type'	: 0,
				oper	: 1,
				host	: '192.168.1.24',
				port	: 8000,
				user	: 'admin',
				pass	: 'Eutroeye'
			}, function(code, desc) {
				frs.ui.body().append($('<div></div>').append('code: ' + code + '; desc: ' + desc));
			});
		}).to_major(),
		new frs.ui.Button('测试 - 海康 - 人形', function() {
			fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
				opp		: 'opp-1',
				did		: 'device-e64392b71a8945cc8c6803aa28852304',
				'dev-type'	: 1,
				'task-type'	: 1,
				oper	: 1,
				host	: '192.168.1.24',
				port	: 8000,
				user	: 'admin',
				pass	: 'Eutroeye'
			}, function(code, desc) {
				frs.ui.body().append($('<div></div>').append('code: ' + code + '; desc: ' + desc));
			});
		}).to_major(),
		$('<br/>'),
		new frs.ui.Button('创建 - 设备 - 在线', function() {
			fomjar.net.send(ski.isis.INST_SET_DEV, {
				did		: 'device-e64392b71a8945cc8c6803aa28852304',
				path	: '南京/新街口/万达广场01',
				host	: '192.168.1.24',
				port	: 8000,
				user	: 'admin',
				pass	: 'Eutroeye'
			}, function(code, desc) {
				frs.ui.body().append($('<div></div>').append('code: ' + code + '; desc: ' + desc));
			});
		}).to_major(),
	]);
}

})(jQuery)
