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

import React from 'react';
import {connect} from "react-redux";
import {ReduxState} from "../../../../reducers";
import CardList from "../../../../components/list/CardList";
import BaseComponent from "../../../../components/BaseComponent";
import {loadCloudHosts} from "../../../../actions";
import {ICloudHost} from "./CloudHost";
import CloudHostCard from "./CloudHostCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    cloudHosts: ICloudHost[];
}

interface DispatchToProps {
    loadCloudHosts: (instanceId?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class CloudHostsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadCloudHosts();
    }

    public render() {
        return (
            <CardList<ICloudHost>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não existem instâncias a executar na cloud"}
                list={this.props.cloudHosts}
                card={this.cloudHost}
                predicate={this.predicate}/>
        )
    }

    private cloudHost = (host: ICloudHost): JSX.Element =>
        <CloudHostCard key={host.instanceId} cloudHost={host}/>;

    private predicate = (host: ICloudHost, search: string): boolean =>
        (host.publicIpAddress && host.publicIpAddress.toLowerCase().includes(search))
        || host.instanceId.includes(search);


}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.hosts.cloud.isLoadingHosts,
        error: state.entities.hosts.cloud.loadHostsError,
        cloudHosts: (state.entities.hosts.cloud.data && Object.values(state.entities.hosts.cloud.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadCloudHosts,
};

export default connect(mapStateToProps, mapDispatchToProps)(CloudHostsList);
