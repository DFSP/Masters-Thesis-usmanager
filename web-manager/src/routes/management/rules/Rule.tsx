import React from "react";
import IDatabaseData from "../../../components/IDatabaseData";

export interface IRule extends IDatabaseData {
  name: string;
  priority: number;
  generic: boolean;
  decision: IDecision;
  conditions: string[];
}

export interface IDecision extends IDatabaseData {
  value: string;
  componentType: IComponentType;
}

interface IComponentType extends IDatabaseData {
  type: string;
}

export const componentTypes = {
  HOST: { type: "host" },
  SERVICE: { type: "service" },
  CONTAINER: { type: "container" }
};

export interface IValueMode extends IDatabaseData {
  name: string;
}

export interface IField extends IDatabaseData {
  name: string;
}

export interface IOperator extends IDatabaseData {
  operator: string;
  symbol: string;
}
