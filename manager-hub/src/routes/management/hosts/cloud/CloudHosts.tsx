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

import React from "react";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import AddButton from "../../../../components/form/AddButton";
import styles from '../edge/EdgeHosts.module.css'
import CloudHostsList from "./CloudHostsList";
import ActionButton from "../../../../components/list/ActionButton";
import BaseComponent from "../../../../components/BaseComponent";
import {syncCloudHosts} from "../../../../actions";
import {connect} from "react-redux";

interface DispatchToProps {
    syncCloudHosts: () => void;
}

type Props = DispatchToProps;

class CloudHosts extends BaseComponent<Props, {}> {

    public render() {
        return (
            <MainLayout>
                <AddButton tooltip={{text: 'Start cloud instance', position: 'bottom'}}
                           pathname={'/hosts/cloud/new_instance?new'}
                           offset={1}/>
                <ActionButton icon={'sync'}
                              tooltip={{text: 'Sync cloud instances', position: 'left'}}
                              clickCallback={this.syncCloudInstances}
                              offset={0}/>
                <div className={`${styles.container}`}>
                    <CloudHostsList/>
                </div>
            </MainLayout>
        );
    }

    private syncCloudInstances = () => {
        this.props.syncCloudHosts();
    };

}

const mapDispatchToProps: DispatchToProps = {
    syncCloudHosts,
};

export default connect(null, mapDispatchToProps)(CloudHosts);
