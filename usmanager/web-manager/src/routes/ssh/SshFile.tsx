/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import BaseComponent from "../../components/BaseComponent";
import Form, {IFields, required} from "../../components/form/Form";
import Field from "../../components/form/Field";
import React from "react";
import {ICloudHost} from "../hosts/cloud/CloudHost";
import {ReduxState} from "../../reducers";
import {loadCloudHosts, loadEdgeHosts} from "../../actions";
import {connect} from "react-redux";
import {IEdgeHost} from "../hosts/edge/EdgeHost";

interface ISshFile {
  hostname: string;
  filename: string;
}

const buildNewSshCommand = (): Partial<ISshFile> => ({
  hostname: undefined,
  filename: undefined,
});

interface StateToProps {
  cloudHosts: { [key: string]: ICloudHost };
  edgeHosts: { [key: string]: IEdgeHost };
}

interface DispatchToProps {
  loadCloudHosts: () => void;
  loadEdgeHosts: () => void;
}

type Props = StateToProps & DispatchToProps;

class SshCommand extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.props.loadEdgeHosts();
    this.props.loadCloudHosts();
  };

  private onPostSuccess = (): void => {
    super.toast(`<span class="green-text">File uploaded</span>`);
  };

  private onPostFailure = (reason: string): void =>
    super.toast(`Failed to upload file`, 10000, reason, true);

  private getFields = (): IFields => (
    {
      hostname: {
        id: 'hostname',
        label: 'hostname',
        validation: { rule: required }
      },
      filename: {
        id: 'filename',
        label: 'filename',
        validation: { rule: required }
      },
    }
  );

  private getSelectableHosts = () => {
    const cloudHosts = Object.values(this.props.cloudHosts).map(instance => instance.publicIpAddress);
    const edgeHosts = Object.keys(this.props.edgeHosts);
    return cloudHosts.concat(edgeHosts);
  };

  private getSelectableFiles = () => {
    //TODO get available scripts from the server instead
    return ['docker-install.sh', 'docker-uninstall.sh', 'node-exporter-install.sh'];
  };

  public render() {
    const command = buildNewSshCommand();
    return (
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
        <Field key='hostname'
               id={'hostname'}
               label='hostname'
               type={'dropdown'}
               dropdown={{
                 defaultValue: 'Select hostname',
                 values: this.getSelectableHosts()}}/>
        <Field key='filename'
               id={'filename'}
               label='filename'
               type={'dropdown'}
               dropdown={{
                 defaultValue: 'Select filename',
                 values: this.getSelectableFiles()}}/>
      </Form>
    );
  }

}

function mapStateToProps(state: ReduxState): StateToProps {
  const cloudHosts = state.entities.hosts.cloud.data;
  const edgeHosts = state.entities.hosts.edge.data;
  return  {
    cloudHosts,
    edgeHosts,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadCloudHosts,
  loadEdgeHosts,
};

export default connect(mapStateToProps, mapDispatchToProps)(SshCommand);
