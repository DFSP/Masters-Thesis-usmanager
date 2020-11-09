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


import React, {createRef} from "react";
import {connect} from "react-redux";
import {INewSshCommand, ISshCommand} from "../../ssh/SshCommand";
import SshPanel, {ICommand, IFileTransfer} from "../../ssh/SshPanel";
import {addCommand} from "../../../../actions";
import BaseComponent from "../../../../components/BaseComponent";
import Field from "../../../../components/form/Field";
import Form, {IFields, requiredAndTrimmed} from "../../../../components/form/Form";
import {IReply} from "../../../../utils/api";
import {ICloudHost} from "./CloudHost";
import {getCloudHostAddress, IHostAddress} from "../Hosts";

const buildNewSshCommand = (): INewSshCommand => ({
    background: false,
    hostAddress: undefined,
    command: undefined,
});

interface CloudHostSshCommandProps {
    cloudHost: ICloudHost;
}

interface DispatchToProps {
    addCommand: (command: ICommand) => void;
}

type Props = CloudHostSshCommandProps & DispatchToProps;

class CloudHostSshCommand extends BaseComponent<Props, {}> {

    private sshPanel = createRef<any>();

    private commandFilter = (command: ICommand | IFileTransfer) =>
        command.hostAddress.publicIpAddress === this.props.cloudHost.publicIpAddress

    public render() {
        const command = buildNewSshCommand();
        return (
            <>
                {/*@ts-ignore*/}
                <Form id={'sshCommand'}
                      fields={this.getFields()}
                      values={command}
                      isNew
                      post={{
                          textButton: 'Executar',
                          url: `hosts/cloud/${this.props.cloudHost?.publicIpAddress}/ssh`,
                          successCallback: this.onPostSuccess,
                          failureCallback: this.onPostFailure
                      }}>
                    <Field key='background'
                           id='background'
                           type='checkbox'
                           checkbox={{label: 'executar em background'}}/>
                    <Field key='command'
                           id='command'
                           label='command'/>
                </Form>
                <SshPanel ref={this.sshPanel} filter={this.commandFilter} hostAddress={getCloudHostAddress(this.props.cloudHost)}/>
            </>
        );
    }

    private onPostSuccess = (reply: IReply<ISshCommand>): void => {
        const command = reply.data;
        const cloudHost = this.props.cloudHost;
        const timestampedCommand: ICommand = {...command, hostAddress: getCloudHostAddress(cloudHost), timestamp: Date.now()};
        this.props.addCommand(timestampedCommand);
        this.sshPanel.current?.scrollToTop();
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Command execution failed`, 10000, reason, true);

    private getFields = (): IFields => (
        {
            background: {
                id: 'background',
                label: 'background',
            },
            command: {
                id: 'command',
                label: 'command',
                validation: {rule: requiredAndTrimmed}
            },
        }
    );

}

const mapDispatchToProps: DispatchToProps = {
    addCommand,
};

export default connect(undefined, mapDispatchToProps)(CloudHostSshCommand);
