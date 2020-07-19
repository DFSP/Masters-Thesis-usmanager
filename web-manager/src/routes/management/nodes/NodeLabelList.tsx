/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import List from "../../../components/list/List";
import React from "react";
import {INode} from "./Node";

interface PortsListProps {
  isLoadingNode: boolean;
  loadNodeError?: string | null;
  node?: INode | null;
}

type Props = PortsListProps;

export default class NodeLabelsList extends BaseComponent<Props, {}> {

  public render() {
    const LabelsList = List<string>();
    return (
      <LabelsList isLoading={this.props.isLoadingNode}
                  error={this.props.loadNodeError}
                  emptyMessage={`No labels associated`}
                  list={this.labels()}
                  show={this.label}/>
    );
  }

  private labels = (): string[] =>
    Object.entries(this.props.node?.labels || []).map(([key, value]) => `${key} = ${value}`);

  private label = (label: string, index: number): JSX.Element =>
    <ListItem key={index} separate={index !== this.labels().length - 1}>
      <div className={`${styles.listItemContent}`}>
        <span>{label}</span>
      </div>
    </ListItem>;

}