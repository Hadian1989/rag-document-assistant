import { TestBed } from '@angular/core/testing';
import { RagApi } from './rag-api';

describe('RagApi', () => {
  let service: RagApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RagApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
