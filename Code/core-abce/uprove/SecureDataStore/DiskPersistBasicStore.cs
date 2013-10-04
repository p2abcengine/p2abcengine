using System;
using System.Collections.Generic;
using System.IO;
using System.IO.MemoryMappedFiles;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Concurrent;
using System.Xml.Serialization;
using System.Runtime.Serialization.Formatters.Binary;

namespace SecureDataStore
{
  /// <summary>
  /// This is a very basic cache to persist data on disk. it is really fast an dump but works.
  /// For as long as there is very few updates/add it works well.
  /// </summary>
  /// <typeparam name="TValue"></typeparam>
  public class DiskPersistBasicStore<TValue>
  {
    [Serializable]
    public class DiskData
    {
      public DiskData(string key, TValue value)
      {
        Key = key;
        Value = value;
      }

      public DiskData() { }

      public string Key { get; set; }
      public TValue Value { get; set; }
    }


    private string _path;
    private BinaryFormatter _xs;
    private string _backingending;

    private static ConcurrentDictionary<string, TValue> _dict = new ConcurrentDictionary<string, TValue>();

    public DiskPersistBasicStore(string path)
    {
      _path = path;
      _xs = new BinaryFormatter();
      _backingending = typeof(TValue).Name;

      //check if path exist
      if (!Directory.Exists(_path))
      {
        throw new Exception("path do not exist.");
      }
      // scan for existing issuer
      IEnumerable<string> files = Directory.GetFiles(_path, "*.*", SearchOption.AllDirectories).Where(s => s.EndsWith(_backingending));
      foreach (string file in files)
      {
        using (FileStream sr = new FileStream(file, FileMode.Open))
        {
          
          DiskData dd = (DiskData)(_xs.Deserialize(sr));
          _dict.TryAdd(dd.Key, dd.Value);
          sr.Close();
        }
      }
    }

    public ICollection<TValue> GetAllValues()
    {
      return _dict.Values;
    }


    public ICollection<string> GetAllKeys()
    {
      return _dict.Keys;
    }


    public void AddValue(string key, TValue value)
    {
      //see if we have an entry with that key already
      if (_dict.ContainsKey(key))
      {
        throw new Exception("already an entry with that key. call replace.");
      }
      _dict.TryAdd(key, value);

      CreateFile(new DiskData(key, value));
    }

    public bool HasValue(string key)
    {
      return _dict.ContainsKey(key);
    }


    public TValue GetValue(string key)
    {
      if (_dict.ContainsKey(key))
      {
        TValue value;
        _dict.TryGetValue(key, out value);
        return value;
      }
      // if not we will try to see if we have one on disk.
      string path = Path.Combine(_path, key + "." + _backingending);
      if (File.Exists(path))
      {
        using (FileStream sr = new FileStream(path, FileMode.Open))
        {
          DiskData dd = (DiskData)(_xs.Deserialize(sr));
          _dict.TryAdd(dd.Key, dd.Value);
          sr.Close();
          return dd.Value;
        }
      }
      throw new Exception("key not found");
    }

    public void RemoveValue(string key)
    {
      string path = Path.Combine(_path, key + "." + _backingending);
      if (File.Exists(path))
      {
        File.Delete(path);
      }

      if (_dict.ContainsKey(key))
      {
        TValue value;
        _dict.TryRemove(key, out value);
      }
    }

    public void UpdateValue(string key, TValue value)
    {
      if (_dict.ContainsKey(key))
      {
        TValue oldValue;
        _dict.TryGetValue(key, out oldValue);
        _dict.TryUpdate(key, value, oldValue);
      }

      string path = Path.Combine(_path, key + "." + _backingending);
      if (File.Exists(path))
      {
        File.Delete(path);
      }
      CreateFile(new DiskData(key, value));
    }

    
    private async void CreateFile(DiskData data)
    {
      string path = Path.Combine(_path, data.Key + "." + _backingending);
      using (FileStream sw = new FileStream(path,FileMode.Create))
      {
        _xs.Serialize(sw, data);
        await sw.FlushAsync();
        sw.Close();
      }
    }






  }




}
