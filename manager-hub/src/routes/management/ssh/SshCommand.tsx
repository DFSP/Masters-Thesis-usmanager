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

import {IReply} from "../../../utils/api";
import BaseComponent from "../../../components/BaseComponent";
import Form, {IFields, required, requiredAndTrimmed} from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import React from "react";
import {awsInstanceStates, ICloudHost} from "../hosts/cloud/CloudHost";
import {ReduxState} from "../../../reducers";
import {connect} from "react-redux";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IHostAddress} from "../hosts/Hosts";

export interface ISshCommand {
    hostAddress: IHostAddress;
    command: string;
    exitStatus: number;
    output: string[];
    error: string[];
}

interface INewSshCommand {
    background?: boolean;
    hostAddress?: IHostAddress;
    command?: string;
}

const buildNewSshCommand = (): INewSshCommand => ({
    background: false,
    hostAddress: undefined,
    command: undefined,
});

interface StateToProps {
    cloudHosts: { [key: string]: ICloudHost };
    edgeHosts: { [key: string]: IEdgeHost };
}

interface SshCommandProps {
    onExecuteCommand: (command: ISshCommand) => void;
}

type Props = SshCommandProps & StateToProps;

class SshCommand extends BaseComponent<Props, {}> {

    public render() {
        const command = buildNewSshCommand();
        return (
            /*@ts-ignore*/
            <Form id={'sshCommand'}
                  fields={this.getFields()}
                  values={command}
                  isNew
                  post={{
                      textButton: 'Execute',
                      url: 'ssh/execute',
                      successCallback: this.onPostSuccess,
                      failureCallback: this.onPostFailure
                  }}>
                <Field key='background'
                       id='background'
                       type='checkbox'
                       checkbox={{label: 'execute in the background'}}/>
                <Field<Partial<IHostAddress>> key='sshHostAddress'
                                              id='hostAddress'
                                              label='hostAddress'
                                              type='dropdown'
                                              dropdown={{
                                                  defaultValue: 'Select host address',
                                                  values: this.getSelectableHosts(),
                                                  optionToString: this.hostAddressesDropdown,
                                                  emptyMessage: 'No hosts available'
                                              }}/>
                <Field key='command'
                       id='command'
                       label='command'/>
            </Form>
        );
    }

    private onPostSuccess = (reply: IReply<ISshCommand>): void => {
        const command = reply.data;
        this.props.onExecuteCommand(command);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Command execution failed`, 10000, reason, true);

    private getFields = (): IFields => (
        {
            background: {
                id: 'background',
                label: 'background',
            },
            hostAddress: {
                id: 'hostAddress',
                label: 'hostAddress',
                validation: {rule: required}
            },
            command: {
                id: 'command',
                label: 'command',
                validation: {rule: requiredAndTrimmed}
            },
        }
    );

    private getSelectableHosts = (): (Partial<IHostAddress>)[] => {
        const cloudHosts = Object.values(this.props.cloudHosts)
            .filter(cloudHost => cloudHost.state.code === awsInstanceStates.RUNNING.code)
            .map(instance => ({
                publicIpAddress: instance.publicIpAddress,
                privateIpAddress: instance.privateIpAddress
            }));
        const edgeHosts = Object.values(this.props.edgeHosts)
            .map(host => ({publicIpAddress: host.publicIpAddress, privateIpAddress: host.privateIpAddress}));
        return [...cloudHosts, ...edgeHosts];
    };

    private hostAddressesDropdown = (hostAddress: Partial<IHostAddress>): string =>
        hostAddress.publicIpAddress + (hostAddress.privateIpAddress ? ("/" + hostAddress.privateIpAddress) : '');

}

function mapStateToProps(state: ReduxState): StateToProps {
    const cloudHosts = state.entities.hosts.cloud.data;
    const edgeHosts = state.entities.hosts.edge.data;
    return {
        cloudHosts,
        edgeHosts,
    }
}

export default connect(mapStateToProps)(SshCommand);
