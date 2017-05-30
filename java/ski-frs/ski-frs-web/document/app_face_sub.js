
(function($) {

fomjar.framework.phase.append('dom', frsmain);
fomjar.framework.phase.append('ren', update);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_sub.html';}).addClass('active');
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var bar = $('<div></div>').append([
        new frs.ui.Button('创建人像库', tool_create).to_major(),
        $("<input placeholder='搜索'>"),
        $("<select><option value='库名'>库名</option><option value='人名'>人名</option><option value='电话'>电话</option><option value='地址'>地址</option><option value='身份证'>身份证</option></select>"),
    ]);
    var head = new frs.ui.ListCellTable(['名称', '主体数量', '创建时间', '操作']);
    var list = new frs.ui.List();
    frs.ui.body().append([bar, $('<div></div>').append([head, list])]);
    
    bar.find('input').bind('keydown', function(e) {
        if (13 == e.keyCode) {
            var type = bar.find('select').val();
            var text = bar.find('input').val().trim();
            tool_search(type, text);
        }
    })
}

function update() {
    var mask = new frs.ui.Mask();
    var hud = frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    
    var list = frs.ui.body().find('.list');
    list.children().detach();
    fomjar.net.send(ski.isis.INST_GET_SUB, function(code, desc) {
        mask.disappear();
        hud.disappear();
        
        if (code) {
            new frs.ui.hud.Minor('获取清单失败: ' + desc).appear(1500);
            return;
        }
        $.each(desc, function(i, sub) {
            var btn_del;
            list.append(new frs.ui.ListCellTable([
                $('<div></div>').append(sub.name),
                $('<div></div>').append(sub.items.length),
                $('<div></div>').append(new Date(sub.time).format('yyyy/MM/dd HH:mm:ss')),
                $('<div></div>').append([
                    new frs.ui.Button('修改', function() {op_modify(sub);}).to_major(),
                    new frs.ui.Button('浏览', function() {op_browse(sub);}).to_major(),
                    new frs.ui.Button('导入', function() {op_import(sub);}).to_major(),
                    btn_del = new frs.ui.Button('删除', function() {op_delete(sub);}).to_major(),
                ]),
            ]));
            btn_del.css('background', '#663333');
        });
    });
}

function tool_create() {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('创建人像库');
    dialog.append_space('.5em');
    dialog.append_input({placeholder : '库名称'});
    dialog.append_button(new frs.ui.Button('提交', function() {
        var name = dialog.find('input').val().trim();
        if (!name) {
            new frs.ui.hud.Minor('库名称不能为空').appear(1500);
            dialog.shake();
            return;
        }
        
        fomjar.net.send(ski.isis.INST_SET_SUB, {
            name : name,
            type : 0
        }, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                dialog.shake();
                return;
            }
            new frs.ui.hud.Minor('创建成功').appear(1500);
            mask.disappear();
            dialog.disappear();
            update();
        });
    }).to_major());
    
    mask.appear();
    dialog.appear();
}

function tool_search(type, text) {
    new frs.ui.hud.Minor(type+':'+text).appear(1500);
}

function op_modify(sub) {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('修改人像库');
    dialog.append_space('.5em');
    dialog.append_input({placeholder : '库名称'});
    dialog.find('input').val(sub.name);
    dialog.append_button(new frs.ui.Button('提交', function() {
        var name = dialog.find('input').val().trim();
        if (!name) {
            new frs.ui.hud.Minor('库名称不能为空').appear(1500);
            dialog.shake();
            return;
        }
        
        fomjar.net.send(ski.isis.INST_MOD_SUB, {
            sid  : sub.sid,
            name : name
        }, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                dialog.shake();
                return;
            }
            new frs.ui.hud.Minor('修改成功').appear(1500);
            mask.disappear();
            dialog.disappear();
            update();
        });
    }).to_major());
    
    mask.appear();
    dialog.appear();
    dialog.find('input');
}

function op_browse(sub) {
    window.location = 'app_face_sub_items.html?sid=' + sub.sid.toString(16);
}

function op_import(sub) {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.css('width', '80%');
    
    var input_path;
    var button_check;
    var button_submit;
    var test_pass = false;
    dialog.append_text_h1c('导入人像');
    dialog.append_space('.5em');
    dialog.append_text_p1('<b>注意：</b><br/>'
                        + '1. 必须是服务端目录，而非当前打开浏览器的本机;<br/>'
                        + '2. 请将人像库提前拷贝至服务端，并解压。');
    input_path = dialog.append_input({placeholder : '请输入人像库所在目录'});
    dialog.append_text_p1('选项：');
    dialog.append_check_input = function(label, placeholder) {
        var c = $("<div><label><input type='checkbox' style='-webkit-appearance : checkbox; appearance : checkbox'>" + label + "</label><input type='text' disabled='disabled' placeholder='" + placeholder + "' style='display : none; width : 100%; text-align : center'></div>");
        var check = c.find('input[type=checkbox]');
        var input = c.find('input[type=text]');
        check.bind('click', function() {
            input[0].disabled = !check[0].checked;
            if (input[0].disabled) {
                input.hide();
            } else {
                input.show();
                input.focus();
            }
        });
        c.check = check;
        c.input = input;
        dialog.append(c);
        return c;
    }
    var oIdno = dialog.append_check_input('提取身份证号', '身份证号正则表达式');
    oIdno.check.trigger('click');
    oIdno.check.attr('disabled', 'disabled');
    oIdno.input.val('_(([1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|x|X))|([1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}))_');
    var oName = dialog.append_check_input('提取姓名', '姓名正则表达式');
    oName.input.val('_(\\W{2,5})_');
    var oPhone = dialog.append_check_input('提取电话', '电话正则表达式');
    oPhone.input.val('_(1[358]\\d{9})_');
    var oAddr = dialog.append_check_input('提取住址', '住址正则表达式');
    oAddr.input.val('_((\\W|\\w|\\d){6,})_');
    var check_result = dialog.append_text_p1();
    check_result.css('font-size', '50%');
    check_result.css('color', '#3333ff');
    dialog.append_buttons([
        button_check  = new frs.ui.Button('测试').to_major(),
        button_submit = new frs.ui.Button('提交').to_disable(),
    ]);
    
    var check_collect = function () {
        var data = {
            key  : new Date().getTime().toString(16),
            sid : sub.sid,
            type : 0,   // man
        };
        var path = input_path.val().trim();
        if (!path) {
            new frs.ui.hud.Minor('目录不能为空').appear(1500);
            dialog.shake();
            return null;
        }
        data.path = path;
        if (oIdno.check[0].checked) {
            var reg_idno = oIdno.input.val().trim();
            if (!reg_idno) {
                new frs.ui.hud.Minor('身份证号正则表达式不能为空').appear(1500);
                dialog.shake();
                return null;
            }
            data.reg_idno = reg_idno;
        }
        if (oName.check[0].checked) {
            var reg_name = oName.input.val().trim();
            if (!reg_name) {
                new frs.ui.hud.Minor('姓名正则表达式不能为空').appear(1500);
                dialog.shake();
                return null;
            }
            data.reg_name = reg_name;
        }
        if (oPhone.check[0].checked) {
            var reg_phone = oPhone.input.val().trim();
            if (!reg_phone) {
                new frs.ui.hud.Minor('电话正则表达式不能为空').appear(1500);
                dialog.shake();
                return null;
            }
            data.reg_phone = reg_phone;
        }
        if (oAddr.check[0].checked) {
            var reg_addr = oAddr.input.val().trim();
            if (!reg_addr) {
                new frs.ui.hud.Minor('住址正则表达式不能为空').appear(1500);
                dialog.shake();
                return null;
            }
            data.reg_addr = reg_addr;
        }
        return data;
    };
    
    button_check.bind('click', function() {
        var data = check_collect();
        if (!data) return;
        
        data.count = 3;
        fomjar.net.send(ski.isis.INST_APPLY_SUB_IMPORT_CHECK, data, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor('一些错误：' + desc).appear(3000);
                dialog.shake();
                return;
            }
            var result = '测试结果：';
            $.each(desc, function(i, r) {
                result += '<br/>文件名：' + r.file;
                if (r.idno) result += '　　身份证：' + r.idno;
                if (r.name) result += '　　姓名：' + r.name;
                if (r.phone) result += '　　电话：' + r.phone;
                if (r.addr) result += '　　住址：' + r.addr;
            });
            check_result.html(result);
            test_pass = true;
            button_submit.to_major();
        });
    });
    var query_loop = function(progress, div_cur, key) {
        fomjar.util.async(function() {
            fomjar.net.send(ski.isis.INST_APPLY_SUB_IMPORT_STATE, {
                key : key
            }, function(code, desc) {
                if (code) {
                    new frs.ui.hud.Minor(desc).appear(1500);
                    return;
                }
                
                div_cur.text(desc.file_current);
                var cur = desc.file_success + desc.file_fails.length;
                var all = desc.file_total
                progress.val(cur / all * 100, cur + ' / ' + all);
                
                if (0 == desc.time_end) {
                    query_loop(progress, div_cur, key);
                } else {
                    dialog.disappear();
                    
                    dialog = new frs.ui.Dialog();
                    dialog.append_text_h1c('导入结束 - 报告如下');
                    var report = '';
                    report += '<br/>开始时间: ' + new Date(desc.time_begin).format('yyyy/MM/dd HH:mm:ss') + '，结束时间: ' + new Date(desc.time_end).format('yyyy/MM/dd HH:mm:ss') + '。';
                    report += '<br/>文件总数: ' + desc.file_total + ' 个，成功: ' + desc.file_success + ' 个，失败: ' + desc.file_fails.length + ' 个。';
                    report += '<br/>移动文件耗时: ' + (desc.time_move / 1000) + ' 秒，分析图片耗时: ' + (desc.time_fv / 1000) + ' 秒。';
                    if (0 < desc.file_fails.length) {
                        report += '<br/>失败文件清单如下:<br/>';
                        report += '<ul>';
                        $.each(desc.file_fails, function(i, file) {
                            report += '<li>' + file + '</li>'
                        });
                        report += '</ul>';
                    }
                    dialog.append_text_p1(report);
                    dialog.append_button(new frs.ui.Button('完成', function() {
                        mask.disappear();
                        dialog.disappear();
                        update();
                    }).to_major());
                    dialog.appear();
                }
            });
        }, 500);
    };
    button_submit.bind('click', function() {
        if (!test_pass) return;
        
        var data = check_collect();
        var key = data.key;
        
        fomjar.net.send(ski.isis.INST_APPLY_SUB_IMPORT, data, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                return;
            }
        
            dialog.disappear();
            mask.unbind('click');
            
            dialog = new frs.ui.Dialog();
            var progress = new frs.ui.Progress();
            dialog.append_text_h1c('正在导入（请不要刷新页面）');
            dialog.append_space('.5em');
            dialog.append($('<div></div>').append(progress));
            var div_cur = dialog.append_text_p1('');
            dialog.appear();
            
            query_loop(progress, div_cur, key);
        });
    });
   
    mask.appear();
    dialog.appear();
}

function op_delete(sub) {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('删除人像库');
    dialog.append_space('.5em');
    dialog.append_text_p1('确定删除人像库: "' + sub.name + '" ?');
    dialog.append_text_h1('其下所有信息都将删除，无法恢复。(图片文件不会删除)');
    dialog.append_button(new frs.ui.Button('确定').to_major()).bind('click', function() {
        var mask1 = new frs.ui.Mask();
        var hud = new frs.ui.hud.Major('正在删除');
        mask1.appear();
        hud.appear();
        
        fomjar.net.send(ski.isis.INST_DEL_SUB, {
            sid : sub.sid
        }, function(code, desc) {
            mask1.disappear();
            hud.disappear();
            
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                dialog.shake();
                return;
            }
            mask.disappear();
            dialog.disappear();
            new frs.ui.hud.Minor("删除成功").appear(1500);
            update();
        });
    });
    
    mask.appear();
    dialog.appear();
}

})(jQuery)

