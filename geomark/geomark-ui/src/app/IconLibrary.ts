import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import {
  faCog,
  faInfo,
  faInfoCircle,
  faGlobeAmericas,
  faCopy,
  faUpload,
  faClipboard,
  faPlusCircle,
  faEdit,
  faTrash,
  faChevronLeft,
  faFileUpload,
  faCheck,
  faTimes,
  faQuestionCircle,
  faMap,
  faDownload,
} from '@fortawesome/free-solid-svg-icons';

const iconsSolid = [
  faCog,
  faInfo,
  faInfoCircle,
  faGlobeAmericas,
  faCopy,
  faUpload,
  faClipboard,
  faPlusCircle,
  faEdit,
  faTrash,
  faChevronLeft,
  faFileUpload,
  faCheck,
  faTimes,
  faQuestionCircle,
  faMap,
  faDownload,
];

export function initIconLibrary(library: FaIconLibrary) {
  for (const icon of iconsSolid) {
    library.addIcons(icon);
  }

}
