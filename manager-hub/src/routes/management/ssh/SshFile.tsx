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
import Form, {IFields, required} from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import React from "react";
import {awsInstanceStates, ICloudHost} from "../hosts/cloud/CloudHost";
import {ReduxState} from "../../../reducers";
import {loadScripts} from "../../../actions";
import {connect} from "react-redux";
import {IEdgeHost} from "../hosts/edge/EdgeHost";
import {IReply} from "../../../utils/api";
import {IHostAddress} from "../hosts/Hosts";

export interface ISshFile {
    hostAddress: IHostAddress;
    filename: string;
}

const buildNewSshCommand = (): Partial<ISshFile> => ({
    hostAddress: undefined,
    filename: undefined,
});

interface StateToProps {
    cloudHosts: { [key: string]: ICloudHost };
    edgeHosts: { [key: string]: IEdgeHost };
    scripts: string[];
}

interface DispatchToProps {
    loadScripts: () => void;
}

interface SshFileProps {
    onTransferFile: (transfer: ISshFile) => void;
}

type Props = SshFileProps & StateToProps & DispatchToProps;

class SshFile extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadScripts();
    };

    public render() {
        const command = buildNewSshCommand();
        return (
            /*@ts-ignore*/
            <Form id={'sshCommand'}
                  fields={this.getFields()}
                  values={command}
                  isNew={true}
                  post={{
                      textButton: 'Upload',
                      url: 'ssh/upload',
                      successCallback: this.onPostSuccess,
                      failureCallback: this.onPostFailure
                  }}>
                <Field<Partial<IHostAddress>> key='hostAddress'
                                              id='hostAddress'
                                              label='hostAddress'
                                              type='dropdown'
                                              dropdown={{
                                                  defaultValue: 'Select host address',
                                                  values: this.getSelectableHosts(),
                                                  optionToString: this.hostAddressesDropdown,
                                                  emptyMessage: 'No hosts available'
                                              }}/>
                <Field key='filename'
                       id='filename'
                       label='filename'
                       type='dropdown'
                       dropdown={{
                           defaultValue: 'Select filename',
                           values: this.props.scripts,
                           emptyMessage: 'No scripts available'
                       }}/>
            </Form>
        );
    }

    private onPostSuccess = (reply: IReply<string>, args: ISshFile): void => {
        super.toast(`<span class="green-text">File uploaded</span>`);
        this.props.onTransferFile(args);
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Failed to upload file`, 10000, reason, true);

    private getFields = (): IFields => (
        {
            hostAddress: {
                id: 'hostAddress',
                label: 'hostAddress',
                validation: {rule: required}
            },
            filename: {
                id: 'filename',
                label: 'filename',
                validation: {rule: required}
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
        hostAddress.publicIpAddress + (hostAddress.privateIpAddress ? ("/" + hostAddress.privateIpAddress) : '') + " - " + hostAddress.coordinates?.label;

}

function mapStateToProps(state: ReduxState): StateToProps {
    const cloudHosts = state.entities.hosts.cloud.data;
    const edgeHosts = state.entities.hosts.edge.data;
    const scripts = state.entities.scripts.data;
    return {
        cloudHosts,
        edgeHosts,
        scripts
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadScripts,
};

export default connect(mapStateToProps, mapDispatchToProps)(SshFile);
