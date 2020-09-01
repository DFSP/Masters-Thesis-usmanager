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

-- Logback: the reliable, generic, fast and flexible logging framework.
-- Copyright (C) 1999-2010, QOS.ch. All rights reserved.
--
-- See http://logback.qos.ch/license.html for the applicable licensing
-- conditions.

-- This SQL script creates the required tables by ch.qos.logback.classic.db.DBAppender.
--
-- It is intended for H2 databases.

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
