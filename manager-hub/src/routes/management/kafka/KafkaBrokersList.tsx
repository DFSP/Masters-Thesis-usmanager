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
import {IKafkaBroker} from "./KafkaBroker";
import BaseComponent from "../../../components/BaseComponent";
import CardList from "../../../components/list/CardList";
import {ReduxState} from "../../../reducers";
import {loadKafkaBrokers} from "../../../actions";
import KafkaBrokerCard from "./KafkaBrokerCard";

interface StateToProps {
    isLoading: boolean
    error?: string | null;
    kafkaBrokers: IKafkaBroker[];
}

interface DispatchToProps {
    loadKafkaBrokers: () => void;
}

type Props = StateToProps & DispatchToProps;

class KafkaBrokersList extends BaseComponent<Props, {}> {

    public componentDidMount(): void {
        this.props.loadKafkaBrokers();
    }

    public render() {
        return (
            <CardList<IKafkaBroker>
                isLoading={this.props.isLoading}
                error={this.props.error}
                emptyMessage={"Não existem kafka brokers a executar"}
                list={this.props.kafkaBrokers}
                card={this.kafkaBroker}
                predicate={this.predicate}/>
        );
    }

    private kafkaBroker = (kafkaBroker: IKafkaBroker): JSX.Element =>
        <KafkaBrokerCard key={kafkaBroker.id} kafkaBroker={kafkaBroker}/>;

    private predicate = (kafkaBroker: IKafkaBroker, search: string): boolean =>
        kafkaBroker.id.toString().includes(search)
        || kafkaBroker.container.publicIpAddress.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        isLoading: state.entities.kafkaBrokers.isLoadingKafkaBrokers,
        error: state.entities.kafkaBrokers.loadKafkaBrokersError,
        kafkaBrokers: (state.entities.kafkaBrokers.data && Object.values(state.entities.kafkaBrokers.data).reverse()) || [],
    }
);

const mapDispatchToProps: DispatchToProps = {
    loadKafkaBrokers,
};

export default connect(mapStateToProps, mapDispatchToProps)(KafkaBrokersList);
