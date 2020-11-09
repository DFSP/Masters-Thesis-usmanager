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
import {IRegistrationServer} from "./RegistrationServer";
import BaseComponent from "../../../components/BaseComponent";
import CardList from "../../../components/list/CardList";
import {ReduxState} from "../../../reducers";
import {loadRegistrationServers} from "../../../actions";
import RegistrationServerCard from "./RegistrationServerCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    registrationServers: IRegistrationServer[];
}

interface DispatchToProps {
    loadRegistrationServers: () => void;
}

type Props = StateToProps & DispatchToProps;

class RegistrationServersList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadRegistrationServers();
    }

    public render() {
        return (
            <CardList<IRegistrationServer>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não há servidores de registo a executar"}
                list={this.props.registrationServers}
                card={this.registrationServer}
                predicate={this.predicate}/>
        );
    }

    private registrationServer = (registrationServer: IRegistrationServer): JSX.Element =>
        <RegistrationServerCard key={registrationServer.containerId} registrationServer={registrationServer}/>;

    private predicate = (registrationServer: IRegistrationServer, search: string): boolean =>
        registrationServer.publicIpAddress.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.registrationServers.isLoadingRegistrationServers,
        error: state.entities.registrationServers.loadRegistrationServersError,
        registrationServers: (state.entities.registrationServers.data && Object.values(state.entities.registrationServers.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadRegistrationServers,
};

export default connect(mapStateToProps, mapDispatchToProps)(RegistrationServersList);
