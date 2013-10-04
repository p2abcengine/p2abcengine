using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SecureDataStore
{
  public class DiskPersistBasicStoreManager<TValue>
  {

    private static volatile DiskPersistBasicStoreManager<TValue> instance;
    private static object syncRoot = new Object();
    private static ConcurrentDictionary<string, DiskPersistBasicStore<TValue>> dict = new ConcurrentDictionary<string, DiskPersistBasicStore<TValue>>();

    private DiskPersistBasicStoreManager() { }

    public static DiskPersistBasicStoreManager<TValue> Instance
    {
      get
      {
        if (instance == null)
        {
          lock (syncRoot)
          {
            if (instance == null)
              instance = new DiskPersistBasicStoreManager<TValue>();
          }
        }

        return instance;
      }
    }


    public DiskPersistBasicStore<TValue> GetDiskPersistBasicStore(string path)
    {
      if (dict.ContainsKey(path))
      {
        return GetValue(path);
      }
      object syncRoot = new Object();
      lock (syncRoot)
      {
        if (dict.ContainsKey(path))
        {
          return GetValue(path);
        }

        DiskPersistBasicStore<TValue> nDiskStore = new DiskPersistBasicStore<TValue>(path); //DiskPersistBasicStoreManagerWorker<TValue>.GetInstance(path);
        dict.TryAdd(path, nDiskStore);
        return nDiskStore;
      }
    }

    private static DiskPersistBasicStore<TValue> GetValue(string path)
    {
      DiskPersistBasicStore<TValue> ret;
      dict.TryGetValue(path, out ret);
      return ret;
    }

  }


  internal class DiskPersistBasicStoreManagerWorker<TValue>
  {
    private static volatile DiskPersistBasicStore<TValue> instance;
    private static object syncRoot = new Object();

    private DiskPersistBasicStoreManagerWorker() { }

    public static DiskPersistBasicStore<TValue> GetInstance(string path)
    {
      if (instance == null)
      {
        lock (syncRoot)
        {
          if (instance == null)
            instance = new DiskPersistBasicStore<TValue>(path);
        }
      }

      return instance;
    }

  }


}
