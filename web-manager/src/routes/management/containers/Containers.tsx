import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import ContainerCard from './ContainerCard';
import AddButton from "../../../components/form/AddButton";
import {connect} from "react-redux";
import {ReduxState} from "../../../reducers";
import CardList from "../../../components/list/CardList";
import {IContainer} from "./Container";
import styles from './Containers.module.css'
import BaseComponent from "../../../components/BaseComponent";
import {loadContainers, reloadContainers} from "../../../actions";
import ActionButton from "../../../components/list/ActionButton";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  containers: IContainer[];
}

interface DispatchToProps {
  loadContainers: () => void;
  reloadContainers: () => void;
}

type Props = StateToProps & DispatchToProps;

class Containers extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.props.loadContainers();
  }

  public render() {
    return (
      <MainLayout>
        <AddButton tooltip={{text: 'Start container', position: 'bottom'}}
                   pathname={'/containers/start_container?new=true'}
                   offset={0}/>
        <ActionButton icon={'sync'}
                      tooltip={{text: 'Sync containers', position: 'left'}}
                      clickCallback={this.reloadContainers}
                      offset={1}/>
        <div className={`${styles.container}`}>
          <CardList<IContainer>
            isLoading={this.props.isLoading}
            error={this.props.error}
            emptyMessage={"No containers to display"}
            list={this.props.containers}
            card={this.container}
            predicate={this.predicate}/>
        </div>
      </MainLayout>
    );
  }

  private container = (container: IContainer): JSX.Element =>
    <ContainerCard key={container.containerId} container={container}/>;

  private predicate = (container: IContainer, search: string): boolean =>
    container.containerId.toString().toLowerCase().includes(search)
    || container.hostname.toLowerCase().includes(search)
    || container.labels['serviceType'].toLowerCase().includes(search);

  private reloadContainers = () => {
    this.props.reloadContainers();
  };

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.containers.isLoadingContainers,
    error: state.entities.containers.loadContainersError,
    containers: (state.entities.containers.data && Object.values(state.entities.containers.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadContainers,
  reloadContainers
};

export default connect(mapStateToProps, mapDispatchToProps)(Containers);
