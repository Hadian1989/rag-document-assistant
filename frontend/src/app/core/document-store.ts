import {DocumentInfo, RagApi} from './rag-api';
import {patchState, signalStore, withComputed, withHooks, withMethods, withState} from '@ngrx/signals';
import {computed, inject} from '@angular/core';


type DocumentState = {
  documents: DocumentInfo[];
  loading: boolean;
  error: string | null;
}

const initialState: DocumentState = {documents: [], loading: false, error: null};

export const DocumentStore = signalStore(
  {providedIn: 'root'},
  withState(initialState),
  withComputed(({documents}) => ({
    count: computed(() => documents().length),
  })),
  withMethods((store, api = inject(RagApi)) => ({
    reload() {
      patchState(store, {loading: true, error: null});
      api.documents().subscribe({
        next: documents => patchState(store, {documents, loading: false}),
        error: () => patchState(store, {loading: false, error: 'Dokumente konnten nicht geladen werden.'}),
      });
    },
  })),
  withHooks({
    onInit(store) {
      store.reload();
    },
  })
)
