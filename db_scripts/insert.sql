INSERT INTO auto_configuration ( name, component, receiver_address, copy_address, enable_mondo, enable_email, daily_run_version, qa, module, labels, sanity, regions, delete_dependency, qalist, enable, service_name)
VALUES ('Auto-Test','Auto-Test','hellenzhu.ai@gmail.com','hellenzhu.ai@gmail.com',true,true,'QA-DailyRun','Hellen','Auto','Auto-Api-Test',true,'UAT',null,null,true,null);

INSERT INTO auto_configuration (name, component, "fastDashboardEnv", "fastKey", "receiverAddress", "copyAddress", "enableFast", "enableMondo", "enableEmail", "fastProject", "dailyRunVersion", qa, module, labels, sanityOnly, regions, delete_dependency, "qaList", migrate, enable, "serviceName", "jiraComponent")
VALUES ('Auto-Test','Auto-Test',null,null,'hellenzhu.ai@gmail.com','hellenzhu.ai@gmail.com',true,true,true,'Auto-Test','QA-DailyRun','Hellen','Auto','Auto-Api-Test',true,'UAT',null,null,true,true, 'user-svc',null);


insert into auto_baseurl(service_name,profile_name,base_url)
insert into auto_baseurl(service_name,profile_name,base_url) values ('user','uat','http://127.0.0.1:8787')
INSERT INTO auto_system_variable ("config_key", "value") VALUES ('automation-tool.service.url', 'https://auto-test.nsroot.com')

insert into auto_endpoint(component,path,method,class_name,service_name,qa_owner) values ('auto_test','/dar/user/login','post','Test_userLogin','user_svc','Hellen')
insert into auto_case(enable,is_e2e,is_sanity,component,component_like,scenario,path,method,profile_name,description,issue_key,label,parameter)
values (true,true,'false','auto_test','','user_login','/dar/user/login','post','uat','Verify should able to login with right auth','','user_login', '{"1":{"path":"/dar/user/login","method":"post","comment":"","request":{"body":{"user":"admin"}},"caseHelp":{},"description":"Verify should able to login with right auth","profileName":"uat","serviceName":"user_svc"}}')
