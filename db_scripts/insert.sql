INSERT INTO auto_configuration ( name, component, daily_run_version, qa, module, labels, sanity, regions, delete_dependency, qalist, enable, service_name)
VALUES ('auto_test','auto_test','QA-DailyRun','Hellen','Auto','Auto-Api-Test',true,'UAT',null,null,true,null);

-- Removed INSERT statement with fast/mondo/email related fields that are no longer supported


insert into auto_baseurl(service_name,profile_name,base_url) values ('user','uat','http://127.0.0.1:8787')
INSERT INTO auto_system_variable ("config_key", "value") VALUES ('automation-tool.service.url', 'https://auto-test.nsroot.com')

insert into auto_endpoint(component,path,method,class_name,service_name,qa_owner) values ('auto_test','/dar/user/login','post','Test_userLogin','user_svc','Hellen')
insert into auto_case(enable,is_e2e,is_sanity,component,component_like,scenario,path,method,profile_name,description,issue_key,label,parameter)
values (true,true,'false','auto_test','','user_login','/dar/user/login','post','uat','Verify should able to login with right auth','','user_login', '{"1":{"path":"/dar/user/login","method":"post","comment":"","request":{"body":{"user":"admin"}},"caseHelp":{},"description":"Verify should able to login with right auth","profileName":"uat","serviceName":"user_svc"}}')
