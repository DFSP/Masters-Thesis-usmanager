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
import {ISshFile} from "../../ssh/SshFile";
import {ICloudHost} from "./CloudHost";
import BaseComponent from "../../../../components/BaseComponent";
import Field from "../../../../components/form/Field";
import Form, {IFields, required} from "../../../../components/form/Form";
import {IReply} from "../../../../utils/api";
import {ReduxState} from "../../../../reducers";
import {addCommand, loadScripts} from "../../../../actions";
import {connect} from "react-redux";
import SshPanel, {ICommand, IFileTransfer} from "../../ssh/SshPanel";
import {getCloudHostAddress, IHostAddress} from "../Hosts";

const buildNewSshCommand = (): Partial<ISshFile> => ({
    hostAddress: undefined,
    filename: undefined,
});

interface StateToProps {
    scripts: string[];
}

interface DispatchToProps {
    loadScripts: () => void;
    addCommand: (command: IFileTransfer) => void;
}

interface SshFileProps {
    cloudHost: ICloudHost;
}

type Props = SshFileProps & StateToProps & DispatchToProps;

class CloudHostSshFileTransfer extends BaseComponent<Props, {}> {

    private sshPanel = createRef<any>();

    public componentDidMount(): void {
        this.props.loadScripts();
    };

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
                      isNew={true}
                      post={{
                          textButton: 'Upload',
                          url: `hosts/cloud/${this.props.cloudHost?.instanceId}/sftp`,
                          successCallback: this.onPostSuccess,
                          failureCallback: this.onPostFailure
                      }}>
                    <Field key='filename'
                           id='filename'
                           label='filename'
                           type='dropdown'
                           dropdown={{
                               defaultValue: 'Selecionar o ficheiro',
                               emptyMessage: 'Não há ficheiros disponíveis',
                               values: this.props.scripts,
                           }}/>
                </Form>
                <SshPanel ref={this.sshPanel} filter={this.commandFilter} hostAddress={getCloudHostAddress(this.props.cloudHost)}/>
            </>
        );
    }

    private onPostSuccess = (reply: IReply<string>, args: ISshFile): void => {
        super.toast(`<span class="green-text">File uploaded</span>`);
        const cloudHost = this.props.cloudHost;
        const transfer = {...args, hostAddress: getCloudHostAddress(cloudHost), timestamp: Date.now()};
        this.props.addCommand(transfer);
        this.sshPanel.current?.scrollToTop();
    };

    private onPostFailure = (reason: string): void =>
        super.toast(`Failed to upload file`, 10000, reason, true);

    private getFields = (): IFields => (
        {
            filename: {
                id: 'filename',
                label: 'filename',
                validation: {rule: required}
            },
        }
    );

}

function mapStateToProps(state: ReduxState): StateToProps {
    const scripts = state.entities.scripts.data;
    return {
        scripts
    }
}

const mapDispatchToProps: DispatchToProps = {
    loadScripts,
    addCommand,
};

export default connect(mapStateToProps, mapDispatchToProps)(CloudHostSshFileTransfer);
