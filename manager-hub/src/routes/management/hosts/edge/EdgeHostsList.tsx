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
import {IEdgeHost} from "./EdgeHost";
import BaseComponent from "../../../../components/BaseComponent";
import {loadEdgeHosts} from "../../../../actions";
import EdgeHostCard from "./EdgeHostCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    edgeHosts: IEdgeHost[];
}

interface DispatchToProps {
    loadEdgeHosts: (hostname?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class EdgeHostsList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadEdgeHosts();
    }

    public render() {
        return (
            <CardList<IEdgeHost>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não há nenhum host edge para mostrar"}
                list={this.props.edgeHosts}
                card={this.edgeHost}
                predicate={this.predicate}/>
        );
    }

    private edgeHost = (host: IEdgeHost): JSX.Element =>
        <EdgeHostCard key={host.id} edgeHost={host}/>;

    private predicate = (host: IEdgeHost, search: string): boolean =>
        (!!host.publicDnsName && host.publicDnsName.toLowerCase().includes(search))
        || (!!host.publicIpAddress && host.publicIpAddress.toLowerCase().includes(search));

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.hosts.edge.isLoadingHosts,
        error: state.entities.hosts.edge.loadHostsError,
        edgeHosts: (state.entities.hosts.edge.data && Object.values(state.entities.hosts.edge.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadEdgeHosts,
};

export default connect(mapStateToProps, mapDispatchToProps)(EdgeHostsList);
