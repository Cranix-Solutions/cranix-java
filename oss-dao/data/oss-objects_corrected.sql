CREATE DATABASE OSS1;
USE OSS1;


CREATE TABLE IF NOT EXISTS Users (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        uid          VARCHAR(32) NOT NULL,
        role         VARCHAR(16) NOT NULL,
        sureName      VARCHAR(64) NOT NULL,
        givenName    VARCHAR(64),
        PRIMARY KEY  (id)
);

INSERT INTO Users VALUES(NULL,'admin','sysadmins','Administrator',NULL);
INSERT INTO Users VALUES(NULL,'tteachers','teachers','TTeachers',NULL);
INSERT INTO Users VALUES(NULL,'tstudents','tstudents','TStudents',NULL);
INSERT INTO Users VALUES(NULL,'tworkstations','sysadmins','TSysadmins',NULL);

CREATE TABLE IF NOT EXISTS Groups (
        id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name        VARCHAR(32) NOT NULL,
        description VARCHAR(64) NOT NULL,
        groupType   CHAR(20)     NOT NULL,
        PRIMARY KEY  (id)
);

INSERT INTO Groups VALUES(NULL,'sysadmins','Sysadmins','primary');
INSERT INTO Groups VALUES(NULL,'teachers','Teachers','primary');
INSERT INTO Groups VALUES(NULL,'students','Students','primary');
INSERT INTO Groups VALUES(NULL,'administration','Administration','primary');
INSERT INTO Groups VALUES(NULL,'workstations','Workstations','primary');
INSERT INTO Groups VALUES(NULL,'templates','Templates','primary');

CREATE TABLE IF NOT EXISTS GroupMember (
        user_id        BIGINT UNSIGNED NOT NULL,
        group_id       BIGINT UNSIGNED NOT NULL,
         FOREIGN KEY(user_id)  REFERENCES Users(id),
        FOREIGN KEY(group_id) REFERENCES Groups(id),
        PRIMARY KEY  (user_id,group_id)
);

CREATE TABLE IF NOT EXISTS Aliases (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        user_id         BIGINT UNSIGNED NOT NULL,
        alias           VARCHAR(64) NOT NULL,
         FOREIGN KEY(user_id)  REFERENCES Users(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS HWConfs (
        id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name          VARCHAR(32) NOT NULL,
        description   VARCHAR(32) NOT NULL,
        deviceType    VARCHAR(16) NOT NULL,
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS Partitions (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        hwconf_id    BIGINT UNSIGNED NOT NULL,
        name         VARCHAR(32) NOT NULL,
        description  VARCHAR(32) NOT NULL,
        OS           VARCHAR(16) NOT NULL,
        joinType     VARCHAR(16) NOT NULL,
        tool         VARCHAR(16) NOT NULL,
        format       VARCHAR(16) NOT NULL,
         FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS Rooms (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        hwconf_id    BIGINT UNSIGNED NOT NULL,
        name         VARCHAR(32) NOT NULL,
        description  VARCHAR(64) NOT NULL,
        roomType     VARCHAR(16) NOT NULL,
        rows         INTEGER  DEFAULT 5,
        places       INTEGER  DEFAULT 5,
        startIP      VARCHAR(16) NOT NULL,
        netMask      INTEGER  NOT NULL,
        FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS Devices (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED NOT NULL,
        hwconf_id    BIGINT UNSIGNED DEFAULT NULL,
        owner_id     BIGINT UNSIGNED DEFAULT NULL,
        name         VARCHAR(32) NOT NULL,
        IP           VARCHAR(16) NOT NULL,
        WLANIP       VARCHAR(16) DEFAULT '',
        MAC          VARCHAR(17) NOT NULL,
        WLANMAC      VARCHAR(17) DEFAULT '',
        deviceType   VARCHAR(16) NOT NULL,
        row          INTEGER  DEFAULT 0,
        place        INTEGER  DEFAULT 0,
        FOREIGN KEY(room_id)   REFERENCES Rooms(id),
        FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
         FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS AccessInRoom (
        id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id        BIGINT UNSIGNED NOT NULL,
        accessType     VARCHAR(8) ,
        FOREIGN KEY(room_id) REFERENCES Rooms(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS AccessInRoomPIT (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        accessinroom_id BIGINT UNSIGNED NOT NULL,
        pointInTime    CHAR(5) DEFAULT '06:00',
	monday         CHAR(1) DEFAULT 'Y',
	tusday         CHAR(1) DEFAULT 'Y',
	wednesday      CHAR(1) DEFAULT 'Y',
	thursday       CHAR(1) DEFAULT 'Y',
	friday         CHAR(1) DEFAULT 'Y',
	saturday       CHAR(1) DEFAULT 'N',
	sunday         CHAR(1) DEFAULT 'N',
	holiday        CHAR(1) DEFAULT 'N',
        FOREIGN KEY(accessinroom_id) REFERENCES AccessInRoom(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS AccessInRoomFW (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        accessinroom_id BIGINT UNSIGNED NOT NULL,
        direct          CHAR(1) DEFAULT 'N',
        logon           CHAR(1) DEFAULT 'Y',
        proxy           CHAR(1) DEFAULT 'Y',
        printing        CHAR(1) DEFAULT 'Y',
        portal          CHAR(1) DEFAULT 'Y',
        
 FOREIGN KEY(accessinroom_id) REFERENCES AccessInRoom(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS AccessInRoomACT (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        accessinroom_id BIGINT UNSIGNED NOT NULL,
	action          VARCHAR(32) DEFAULT '',
        
        FOREIGN KEY(accessinroom_id) REFERENCES AccessInRoom(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS LoggedOn (
        user_id      BIGINT UNSIGNED NOT NULL,
        device_id    BIGINT UNSIGNED NOT NULL,
       
       FOREIGN KEY(user_id)   REFERENCES Users(id),
        
        FOREIGN KEY(device_id) REFERENCES Devices(id),
        PRIMARY KEY  (device_id, user_id)
);

CREATE TABLE IF NOT EXISTS DefaultPrinters (
        room_id      BIGINT UNSIGNED NOT NULL,
        device_id    BIGINT UNSIGNED NOT NULL,
        printer_id   BIGINT UNSIGNED NOT NULL,
     
     FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        FOREIGN KEY(device_id)  REFERENCES Devices(id),
  FOREIGN KEY(printer_id) REFERENCES Devices(id) ,
   PRIMARY KEY  (device_id, room_id)
);

CREATE TABLE IF NOT EXISTS AvailablePrinters (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED DEFAULT NULL,
        device_id    BIGINT UNSIGNED DEFAULT NULL,
        printer_id   BIGINT UNSIGNED NOT NULL,
           FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        FOREIGN KEY(device_id)  REFERENCES Devices(id),
         FOREIGN KEY(printer_id) REFERENCES Devices(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS Acls (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        object_id    BIGINT UNSIGNED NOT NULL,
        target_id    BIGINT UNSIGNED,
        targetType   VARCHAR(32)  NOT NULL,
        acl          VARCHAR(32)  NOT NULL,
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS  Tests (
        id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        testName      VARCHAR(128) NOT NULL,
        teacher_id    BIGINT UNSIGNED NOT NULL,
        room_id       BIGINT UNSIGNED,
        testDir       VARCHAR(128) NOT NULL,
        currentStep   VARCHAR(128) NOT NULL,
        startTime     DATETIME NOT NULL,
        endTime       DATETIME NOT NULL,
        login         CHAR(1) NOT NULL DEFAULT 'Y',
        proxy         CHAR(1) NOT NULL DEFAULT 'N',
        direct        CHAR(1) NOT NULL DEFAULT 'N',
        portal        CHAR(1) NOT NULL DEFAULT 'N',
        FOREIGN KEY(teacher_id) REFERENCES Users(id),
        FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS TestFiles (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        test_id      BIGINT UNSIGNED NOT NULL,
        user_id      BIGINT UNSIGNED NOT NULL,
        getOrPost    VARCHAR(128) NOT NULL,
        fileName     VARCHAR(256) NOT NULL,
        dateTime     DATETIME NOT NULL,
         FOREIGN KEY(test_id) REFERENCES Tests(id),
        FOREIGN KEY(user_id) REFERENCES Users(id),
        PRIMARY KEY  (id)
);

CREATE TABLE IF NOT EXISTS TestUsers (
        test_id      BIGINT UNSIGNED NOT NULL,
        user_id      BIGINT UNSIGNED NOT NULL,
        device_id    BIGINT UNSIGNED,
        FOREIGN KEY(test_id) REFERENCES Tests(id),
        FOREIGN KEY(user_id) REFERENCES Users(id),
         FOREIGN KEY(device_id) REFERENCES Devices(id),
        PRIMARY KEY (test_id,user_id)
);

CREATE TABLE IF NOT EXISTS Enumerates (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name         VARCHAR(32)  NOT NULL,
        value        VARCHAR(32)  NOT NULL,
        PRIMARY KEY  (id)
);

INSERT INTO Enumerates VALUES(NULL,'deviceType','FatClient');
INSERT INTO Enumerates VALUES(NULL,'deviceType','ThinClient');
INSERT INTO Enumerates VALUES(NULL,'deviceType','ManagedMobileDevice');
INSERT INTO Enumerates VALUES(NULL,'deviceType','MobileDevice');
INSERT INTO Enumerates VALUES(NULL,'deviceType','Printer');
INSERT INTO Enumerates VALUES(NULL,'deviceType','Switch');
INSERT INTO Enumerates VALUES(NULL,'deviceType','Router');
INSERT INTO Enumerates VALUES(NULL,'role','students');
INSERT INTO Enumerates VALUES(NULL,'role','teachers');
INSERT INTO Enumerates VALUES(NULL,'role','sysadmins');
INSERT INTO Enumerates VALUES(NULL,'role','administration');
INSERT INTO Enumerates VALUES(NULL,'role','workstations');
INSERT INTO Enumerates VALUES(NULL,'groupType','primary');
INSERT INTO Enumerates VALUES(NULL,'groupType','class');
INSERT INTO Enumerates VALUES(NULL,'groupType','workgroup');
INSERT INTO Enumerates VALUES(NULL,'groupType','guest');
INSERT INTO Enumerates VALUES(NULL,'roomType','ClassRoom');
INSERT INTO Enumerates VALUES(NULL,'roomType','ComputerRoom');
INSERT INTO Enumerates VALUES(NULL,'roomType','Library');
INSERT INTO Enumerates VALUES(NULL,'roomType','Laboratory');
INSERT INTO Enumerates VALUES(NULL,'accessType','DEFAULT');
INSERT INTO Enumerates VALUES(NULL,'accessType','FW');
INSERT INTO Enumerates VALUES(NULL,'accessType','ACT');


#Some additional config tables
CREATE TABLE IF NOT EXISTS UserConfig (
        user_id      BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
        PRIMARY KEY  (user_id,`key`,value)
);

CREATE TABLE IF NOT EXISTS UserMConfig (
        user_id      BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
          PRIMARY KEY  (user_id,`key`)
);

CREATE TABLE IF NOT EXISTS GroupConfig (
        group_id     BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
          PRIMARY KEY  (group_id,`key`,value)
);

CREATE TABLE IF NOT EXISTS GroupMConfig (
        group_id     BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
         PRIMARY KEY  (group_id,`key`)
);

CREATE TABLE IF NOT EXISTS DeviceConfig (
        device_id    BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
      PRIMARY KEY  (device_id,`key`,value)
);

CREATE TABLE IF NOT EXISTS DeviceMConfig (
        device_id    BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
        PRIMARY KEY  (device_id,`key`)
);

CREATE TABLE IF NOT EXISTS RoomConfig (
        room_id      BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
         PRIMARY KEY  (room_id,`key`,value)
);

CREATE TABLE IF NOT EXISTS RoomMConfig (
        room_id      BIGINT UNSIGNED NOT NULL,
        `key`          VARCHAR(256) NOT NULL,
        value        VARCHAR(256) NOT NULL,
         PRIMARY KEY  (room_id,`key`)
);

CREATE TABLE IF NOT EXISTS Session (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT unsigned NOT NULL,
  room_id      BIGINT UNSIGNED  NULL,
  device_id BIGINT UNSIGNED NULL,
  createdate timestamp not null,
  ip varchar(30),
  token varchar(60),
   FOREIGN KEY(user_id) REFERENCES Users(id),
   FOREIGN KEY(room_id)   REFERENCES Rooms(id),
   FOREIGN KEY(device_id) REFERENCES DEVICES(id),
 PRIMARY KEY (`id`)
 );

