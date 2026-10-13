-- 更新admin用户密码为 123456 的BCrypt哈希
-- BCrypt hash for '123456' = $2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7G9hM0YxKhPkJAG.Mrq7G9hM0YxKh

UPDATE `user`
SET `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7G9hM0YxKhPkJAG.Mrq7G9hM0YxKh'
WHERE `username` = 'admin';

UPDATE `user`
SET `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7G9hM0YxKhPkJAG.Mrq7G9hM0YxKh'
WHERE `username` = 'test1';

UPDATE `user`
SET `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7G9hM0YxKhPkJAG.Mrq7G9hM0YxKh'
WHERE `username` = 'test2';