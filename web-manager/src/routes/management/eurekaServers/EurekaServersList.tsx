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
import {IEurekaServer} from "./EurekaServer";
import BaseComponent from "../../../components/BaseComponent";
import CardList from "../../../components/list/CardList";
import {ReduxState} from "../../../reducers";
import {loadEurekaServers} from "../../../actions";
import EurekaServerCard from "./EurekaServerCard";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  eurekaServers: IEurekaServer[];
}

interface DispatchToProps {
  loadEurekaServers: () => void;
}

type Props = StateToProps & DispatchToProps;

class EurekaServersList extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.props.loadEurekaServers();
  }

  public render() {
    return (
      <CardList<IEurekaServer>
        isLoading={this.props.isLoading}
        error={this.props.error}
        emptyMessage={"No eureka servers to display"}
        list={this.props.eurekaServers}
        card={this.eurekaServer}
        predicate={this.predicate}/>
    );
  }

  private eurekaServer = (eurekaServer: IEurekaServer): JSX.Element =>
    <EurekaServerCard key={eurekaServer.containerId} eurekaServer={eurekaServer}/>;

  private predicate = (eurekaServer: IEurekaServer, search: string): boolean =>
    eurekaServer.hostname.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.eurekaServers.isLoadingEurekaServers,
    error: state.entities.eurekaServers.loadEurekaServersError,
    eurekaServers: (state.entities.eurekaServers.data && Object.values(state.entities.eurekaServers.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadEurekaServers,
};

export default connect(mapStateToProps, mapDispatchToProps)(EurekaServersList);
