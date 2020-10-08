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

import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import List from "../../../components/list/List";
import React from "react";
import {IContainer, IContainerPort} from "./Container";

interface ContainerPortsListProps {
    isLoadingContainer: boolean;
    loadContainerError?: string | null;
    container?: IContainer | null;
}

type Props = ContainerPortsListProps;

export default class ContainerPortsList extends BaseComponent<Props, {}> {

    public render() {
        const PortsList = List<IContainerPort>();
        return (
            <PortsList isLoading={this.props.isLoadingContainer}
                       error={this.props.loadContainerError}
                       emptyMessage={`No ports associated`}
                       list={this.ports()}
                       show={this.port}/>
        );
    }

    private ports = () =>
        this.props.container?.ports || [];

    private port = (port: IContainerPort, index: number): JSX.Element =>
        <ListItem key={index} separate={index !== this.ports().length - 1}>
            <div className={`${styles.listItemContent}`}>
                <span>{port.privatePort}:{port.publicPort} {port.ip}/{port.type}</span>
            </div>
        </ListItem>;

}
