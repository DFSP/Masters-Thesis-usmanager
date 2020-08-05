/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

# Logback: the reliable, generic, fast and flexible logging framework.
# Copyright (C) 1999-2010, QOS.ch. All rights reserved.
#
# See http://logback.qos.ch/license.html for the applicable licensing
# conditions.

# This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender.
#
# It is intended for H2 databases.

DROP TABLE logging_event_exception IF EXISTS;
DROP TABLE logging_event_property IF EXISTS;
DROP TABLE logging_event IF EXISTS;

CREATE TABLE logging_event
(
    timestmp BIGINT NOT NULL,
    formatted_message LONGVARCHAR NOT NULL,
    logger_name VARCHAR(256) NOT NULL,
    level_string VARCHAR(256) NOT NULL,
    thread_name VARCHAR(256),
    reference_flag SMALLINT,
    arg0 VARCHAR(256),
    arg1 VARCHAR(256),
    arg2 VARCHAR(256),
    arg3 VARCHAR(256),
    caller_filename VARCHAR(256),
    caller_class VARCHAR(256),
    caller_method VARCHAR(256),
    caller_line CHAR(4),
    event_id IDENTITY NOT NULL
);


CREATE TABLE logging_event_property
(
    event_id BIGINT NOT NULL,
    mapped_key  VARCHAR(254) NOT NULL,
    mapped_value LONGVARCHAR,
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
);

CREATE TABLE logging_event_exception
(
    event_id BIGINT NOT NULL,
    i SMALLINT NOT NULL,
    trace_line VARCHAR(256) NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
);


# Symmetricds configuration

------------------------------------------------------------------------------
-- Clear and load SymmetricDS Configuration
------------------------------------------------------------------------------

delete from sym_trigger_router;
delete from sym_trigger;
delete from sym_router;
delete from sym_node_group_link;
delete from sym_node_group;
delete from sym_node_host;
delete from sym_node_identity;
delete from sym_node_security;
delete from sym_node;

------------------------------------------------------------------------------
-- Node Groups
------------------------------------------------------------------------------

insert into sym_node_group (node_group_id, description)
values ('master-manager', 'A master manager node. Available at the cloud.');
insert into sym_node_group (node_group_id, description)
values ('worker-manager', 'A worker manager node. Available at the edge.');

------------------------------------------------------------------------------
-- Node Group Links
------------------------------------------------------------------------------

-- master-manager sends changes to worker-manager when worker pulls from the master
insert into sym_node_group_link (source_node_group_id, target_node_group_id, data_event_action)
values ('master-manager', 'worker-manager', 'W');

-- worker-manager sends changes to master-manager when worker pushes to the master
insert into sym_node_group_link (source_node_group_id, target_node_group_id, data_event_action)
values ('worker-manager', 'master-manager', 'P');

------------------------------------------------------------------------------
-- Triggers
------------------------------------------------------------------------------

insert into sym_trigger (trigger_id, source_table_name, channel_id, last_update_time, create_time)
values ('apps', 'apps', 'default', current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id, source_table_name, channel_id, last_update_time, create_time)
values ('rules', 'rules', 'default', current_timestamp, current_timestamp);

------------------------------------------------------------------------------
-- Routers
------------------------------------------------------------------------------

-- Default router sends all data from master to workers
insert into sym_router (router_id, source_node_group_id, target_node_group_id, router_type, create_time, last_update_time)
values ('master-to-worker', 'master-manager', 'worker-manager', 'default', current_timestamp, current_timestamp);

-- Column match router will subset data from master-manager to a specific worker
insert into sym_router (router_id, source_node_group_id, target_node_group_id, router_type, router_expression, create_time, last_update_time)
values ('master-to-one-worker', 'master-manager', 'worker-manager', 'column', 'WORKER_ID=:EXTERNAL_ID or OLD_WORKER_ID=:EXTERNAL_ID', current_timestamp, current_timestamp);

-- Default router sends all data from workers to manager
insert into sym_router (router_id, source_node_group_id, target_node_group_id, router_type, create_time, last_update_time)
values('worker-to-master', 'worker-manager', 'master-manager', 'default', current_timestamp, current_timestamp);

------------------------------------------------------------------------------
-- Trigger Routers
------------------------------------------------------------------------------

insert into sym_trigger_router (trigger_id, router_id, initial_load_order, create_time, last_update_time)
values ('apps', 'master-to-worker', 1, current_timestamp, current_timestamp);

insert into sym_trigger_router (trigger_id, router_id, initial_load_order, initial_load_select, create_time, last_update_time)
values ('rules', 'master-to-one-worker', 1, 'worker_id=''$(externalId)''', current_timestamp, current_timestamp);

insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time)
values('apps', 'worker-to-master', 2, current_timestamp, current_timestamp);

insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time)
values('rules', 'worker-to-master', 2, current_timestamp, current_timestamp);

------------------------------------------------------------------------------
-- Nodes
------------------------------------------------------------------------------

insert into sym_node (node_id, node_group_id, external_id, sync_enabled, created_at_node_id)
values ('001', 'worker-manager', '001', 1, '000');
insert into sym_node (node_id, node_group_id, external_id, sync_enabled, created_at_node_id)
values ('002', 'worker-manager', '002', 1, '000');

insert into sym_node_security (node_id, node_password, registration_enabled, initial_load_enabled, created_at_node_id)
values ('001', 'Gr8hzjQCvQxzwJPhaVQ4cYAJLwaEgC', 1, 1, '000');
insert into sym_node_security (node_id, node_password, registration_enabled, initial_load_enabled, created_at_node_id)
values ('002', 'Gr8hzjQCvQxzwJPhaVQ4cYAJLwaEgC', 1, 1, '000');