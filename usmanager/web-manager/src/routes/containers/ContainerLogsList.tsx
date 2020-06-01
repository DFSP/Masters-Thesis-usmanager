import BaseComponent from "../../components/BaseComponent";
import ListItem from "../../components/list/ListItem";
import styles from "./ContainerLogsList.module.css";
import List from "../../components/list/List";
import React from "react";
import {IContainer} from "./Container";
import {ReduxState} from "../../reducers";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {loadContainerLogs} from "../../actions";

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  logs?: string;
}

interface DispatchToProps {
  loadContainerLogs: (containerId: string) => void;
}

interface PortsListProps {
  isLoadingContainer: boolean;
  loadContainerError?: string | null;
  container?: IContainer | null;
}

type Props = StateToProps & DispatchToProps & PortsListProps;

class ContainerLogsList extends BaseComponent<Props, {}> {

  componentDidMount(): void {
    this.loadEntities();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
    if (prevProps.container?.containerId !== this.props.container?.containerId) {
      this.loadEntities();
    }
  }

  private loadEntities = () => {
    this.reloadLogs();
  };

  private reloadLogs = () => {
    if (this.props.container?.containerId) {
      const {containerId} = this.props.container;
      this.props.loadContainerLogs(containerId);
    }
  };

  private logs = () => {
    let logs = this.props.container?.logs?.split("\n");
    if (logs) {
      // pop 1 to remove the last \n
      logs.pop();
    }
    else {
      logs = [];
    }
    return logs;
  };

  private log = (logs: string, index: number): JSX.Element =>
    <ListItem key={index}>
      <div className={styles.listItemContent}>
        <span>{logs}</span>
      </div>
    </ListItem>;

  public render() {
    const logs = this.logs();
    const LogsList = List<string>();
    return (
      <>
        {!!logs.length && (
          <div className='smallControlsContainer'>
          <button className='btn-floating btn-flat btn-small waves-effect waves-light right tooltipped'
                  data-position="left"
                  data-tooltip="Reload"
                  type="button"
                  onClick={this.reloadLogs}>
            <i className="large material-icons">cached</i>
          </button>
        </div>)}
        <div className={styles.logsListContainer}>
          <LogsList isLoading={this.props.isLoadingContainer || this.props.isLoading}
                    error={this.props.loadContainerError || this.props.error}
                    emptyMessage={`No logs available`}
                    list={logs}
                    show={this.log}
                    paginate={{pagesize: {initial: 50}, position: 'top-bottom'}}/>
        </div>
      </>

    );
  }

}

function mapStateToProps(state: ReduxState, ownProps: PortsListProps): StateToProps {
  const containerId = ownProps.container?.containerId;
  const container = containerId && state.entities.containers.data[containerId];
  const logs = container && container.logs;
  return {
    isLoading: state.entities.containers.isLoadingLogs,
    error: state.entities.containers.loadLogsError,
    logs: logs
  }
}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
  bindActionCreators({
    loadContainerLogs
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ContainerLogsList);