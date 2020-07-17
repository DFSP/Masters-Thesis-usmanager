import React from "react";
import MainLayout from "../../../views/mainLayout/MainLayout";
import LogsList from "./LogsList";

export interface ILogs {
  // Names come from dbappender plugin of logback. DON'T CHANGE THEM
  timestmp: number;
  formattedMessage: string;
  loggerName: string;
  levelString: string;
  threadName: string;
  referenceFlag: number;
  arg0: string;
  arg1: string;
  arg2: string;
  arg3: string;
  callerFilename: string;
  callerClass: string;
  callerMethod: string;
  callerLine: string;
  eventId: number;
}

const ManagementLogs = () => (
  <MainLayout>
    <LogsList/>
  </MainLayout>
);

export default ManagementLogs;
