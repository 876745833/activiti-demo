-- ������sql
drop table users;
drop table roles;
drop table userrole;
CREATE TABLE act.users(id int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,name varchar(30),age varchar(20),password varchar(30));
CREATE TABLE act.roles(id int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT,roleName varchar(30));
CREATE TABLE act.userrole(userId int(11),roleId int(11));

-- ��������
INSERT INTO act.users (id, name, age, password) VALUES (1, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (2, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (3, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (4, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (5, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (6, '����', '25', 'admin');
INSERT INTO act.users (id, name, age, password) VALUES (7, '����Ա', '25', 'admin');

INSERT INTO act.roles (id, roleName) VALUES (1, '��������');
INSERT INTO act.roles (id, roleName) VALUES (2, '���ž���');
INSERT INTO act.roles (id, roleName) VALUES (3, 'CTO');
INSERT INTO act.roles (id, roleName) VALUES (4, '����Ա');

INSERT INTO act.userrole (userId, roleId) VALUES (1, 1);
INSERT INTO act.userrole (userId, roleId) VALUES (2, 1);
INSERT INTO act.userrole (userId, roleId) VALUES (3, 2);
INSERT INTO act.userrole (userId, roleId) VALUES (4, 2);
INSERT INTO act.userrole (userId, roleId) VALUES (5, 3);
INSERT INTO act.userrole (userId, roleId) VALUES (6, 3);
INSERT INTO act.userrole (userId, roleId) VALUES (7, 4);

-- ��������ͼҪ��֤��������һ�£������û���ACT_ID_MEMBERSHIP��������ֶζ����ַ��ͣ�һ��ϵͳ�ж�����NUMBER��Ϊ��
-- ������ɫ���������ͣ����Դ�����ͼ��ʱ��Ҫ����������ת��Ϊ�ַ��͡�

-- ACT_ID_USER
CREATE OR REPLACE VIEW ACT_ID_USER_hhh
(id_, rev_, first_, last_, email_, pwd_, picture_id_) as
  SELECT u.id,1,u.name,null,null,u.password,null FROM users u;
-- ACT_ID_GROUP
create or replace view act_id_group
(id_, rev_, name_, type_) as select t.roleName , 1 as rev_,t.roleName ,'' as type_ from roles t;
-- ACT_ID_MEMBERSHIP
create or replace view act_id_membership
(user_id_, group_id_) as select u.name,r.roleName from
users u inner join userrole ur
on u.id=ur.userId inner join roles r on ur.roleId=r.id;



-- select * from users u,roles r inner join userrole ur where u.id = ur.userId and r.id=ur.roleId;
