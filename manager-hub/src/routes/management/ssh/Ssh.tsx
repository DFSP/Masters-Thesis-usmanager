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

import MainLayout from "../../../views/mainLayout/MainLayout";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import React, {createRef} from "react";
import SshCommand, {ISshCommand} from "./SshCommand";
import SshFile, {ISshFile} from "./SshFile";
import styles from "./Ssh.module.css";
import {connect} from "react-redux";
import {addCommand, loadCloudHosts, loadEdgeHosts} from "../../../actions";
import {ICommand, IFileTransfer} from "./SshPanel";
import SshPanel from "./SshPanel";

interface DispatchToProps {
    loadCloudHosts: () => void;
    loadEdgeHosts: () => void;
    addCommand: (command: ICommand | IFileTransfer) => void;
}

type Props = DispatchToProps;

class Ssh extends React.Component<Props, {}> {

    private sshPanel = createRef<any>();

    public componentDidMount() {
        this.props.loadEdgeHosts();
        this.props.loadCloudHosts();
    }

    public render() {
        return (
            <MainLayout>
                <div className={`container ${styles.tabsContainer}`}>
                    <Tabs tabs={this.tabs()}/>
                </div>
                <SshPanel ref={this.sshPanel}/>
            </MainLayout>
        );
    }

    private tabs = (): Tab[] => [
        {
            title: 'Execute command',
            id: 'executeCommand',
            content: () => <SshCommand onExecuteCommand={this.addCommand}/>
        },
        {
            title: 'Upload file',
            id: 'uploadFile',
            content: () => <SshFile onTransferFile={this.addFileTransfer}/>
        }
    ];

    private addCommand = (sshCommand: ISshCommand) => {
        const command = {...sshCommand, timestamp: Date.now()};
        this.props.addCommand(command);
        this.sshPanel.current?.scrollToTop();
    }

    private addFileTransfer = (fileTransfer: ISshFile) => {
        const transfer = {...fileTransfer, timestamp: Date.now()};
        this.props.addCommand(transfer);
        this.sshPanel.current?.scrollToTop();
    }

}

const mapDispatchToProps: DispatchToProps = {
    addCommand,
    loadCloudHosts,
    loadEdgeHosts,
};

export default connect(undefined, mapDispatchToProps)(Ssh);