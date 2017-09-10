CREATE TABLE user_profile (
    id bigint PRIMARY KEY,
    name text,
    allow_public_message boolean
);

CREATE TABLE user_password (
    user_id bigint REFERENCES user_profile(id) PRIMARY KEY,
    pswhash text
);

INSERT into user_password (user_id, pswhash) VALUES (?, crypt(?, gen_salt('bf')));
SELECT user_id FROM user_password WHERE pswhash = crypt(?, pswhash);
CREATE TABLE user_session(
    user_id big_int REFERENCES user_profile(id) PRIMARY KEY,
    expiration timestamp
);

CREATE TABLE user_relationships_request (
    user_id_1 big_int REFERENCES user_profile(id),
    user_id_2 big_int REFERENCES user_profile(id),
    initiated timestamp,
    initiator big_int REFERENCES user_profile(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id_1, user_id_2)
);

CREATE TABLE user_relationships (
    user_id_1 big_int REFERENCES user_profile(id),
    user_id_2 big_int REFERENCES user_profile(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id_1, user_id_2)
);

CREATE TABLE user_block_list (
    user_id big_int REFERENCES user_profile(id),
    blocked_user_id big_int REFERENCES user_profile(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, blocked_user_id)
);

CREATE TABLE chat_group (
    id big_int PRIMARY KEY,
    name text,
    read_public boolean,
    write_public boolean,
    add_member_public boolean,
    modify_group_public boolean
);

CREATE TABLE user_access_chat_group_read (
    user_id big_int REFERENCES user_profile(id),
    chat_group_id big_int REFERENCES chat_group(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, chat_group_id)
);

CREATE TABLE user_access_chat_group_write (
    user_id big_int REFERENCES user_profile(id),
    chat_group_id big_int REFERENCES chat_group(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, chat_group_id)
)

CREATE TABLE user_access_chat_group_add_member (
    user_id big_int REFERENCES user_profile(id),
    chat_group_id big_int REFERENCES chat_group(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, chat_group_id)
)

CREATE TABLE user_access_chat_group_modify (
    user_id big_int REFERENCES user_profile(id),
    chat_group_id big_int REFERENCES chat_group(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, chat_group_id)
)

CREATE TABLE user_access_chat_group_security (
    user_id big_int REFERENCES user_profile(id),
    chat_group_id big_int REFERENCES chat_group(id),
    CONSTRAINT user_relationships_request_pk PRIMARY KEY (user_id, chat_group_id)
)


--##chat rooms
--##read access (pub, priv)
--##write access (pub, priv)
--##add members access (pub, priv)
--##modify group details access (pub, priv)
--##modify group security access (priv)
--
--
