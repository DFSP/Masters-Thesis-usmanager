/*
 * Copyright (c) 2020 usmanager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import React from 'react';
import {connect} from "react-redux";
import {ILoadBalancer} from "./LoadBalancer";
import BaseComponent from "../../components/BaseComponent";
import CardList from "../../components/list/CardList";
import {ReduxState} from "../../reducers";
import {loadLoadBalancers} from "../../actions";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  loadBalancers: ILoadBalancer[];
}

interface DispatchToProps {
  loadLoadBalancers: () => any;
}

type Props = StateToProps & DispatchToProps;

class LoadBalancersList extends BaseComponent<Props, {}> {

  componentDidMount(): void {
    this.props.loadLoadBalancers();
  }

  private loadBalancer = (loadBalancer: ILoadBalancer): JSX.Element =>
    <div></div>
    //<EdgeHostCard key={host.id} edgeHost={host}/>;

  private predicate = (loadBalancer: ILoadBalancer, search: string): boolean =>
    true
    //host.hostname.toLowerCase().includes(search);

  render = () =>
    <CardList<ILoadBalancer>
      isLoading={this.props.isLoading}
      error={this.props.error}
      emptyMessage={"No load balancers to display"}
      list={this.props.loadBalancers}
      card={this.loadBalancer}
      predicate={this.predicate}/>

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: false, //state.entities.hosts.edge.isLoadingHosts,
    error: null, //state.entities.hosts.edge.loadHostsError,
    loadBalancers: /*(state.entities.hosts.edge.data && Object.values(state.entities.hosts.edge.data)) ||*/ [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadLoadBalancers,
};

export default connect(mapStateToProps, mapDispatchToProps)(LoadBalancersList);
