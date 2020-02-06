/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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
import MainLayout from '../shared/MainLayout';
import ServiceCard from './ServiceCard';

import AddButton from "../shared/AddButton";
import {connect} from "react-redux";
import {loadServices} from "../../actions";
import {ReduxState} from "../../reducers";
import CardList from "../shared/CardList";
import {IService} from "./Service";
import './Services.css'

interface StateToProps {
    services: IService[];
    error: string;
}

interface DispatchToProps {
    loadServices: (name?: string, requiredField?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class Services extends React.Component<Props, {}> {

    public componentDidMount = () =>
        this.props.loadServices();

    private empty = (): JSX.Element =>
        <h1><i>empty</i></h1>; //TODO

    private service = (service: IService): JSX.Element =>
        <ServiceCard key={service.id} service={service} />;

    private predicate = (service: IService, search: string): boolean =>
        service.serviceName.includes(search);

    render = () =>
        <MainLayout>
            <AddButton tooltip={'Add service'} pathname={'/services/new'}/>
            <div className="services-container">
                <CardList<IService>
                    empty={this.empty}
                    list={this.props.services}
                    card={this.service}
                    predicate={this.predicate}/>
            </div>
        </MainLayout>
}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        services: Object.values(state.entities.services),
        error: state.ui.errorMessage,
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadServices,
};

export default connect(mapStateToProps, mapDispatchToProps)(Services);