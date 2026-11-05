from pathlib import Path
import importlib.util,tempfile,subprocess,os,argparse
ROOT=Path(__file__).resolve().parents[1];REPO=ROOT.parents[1]
p=argparse.ArgumentParser();p.add_argument('--input',type=Path);args=p.parse_args()
spec=importlib.util.spec_from_file_location('connection_builder',ROOT/'build_script.py');builder=importlib.util.module_from_spec(spec);spec.loader.exec_module(builder)
base=args.input.read_bytes() if args.input else (REPO/'Script_Completo_Hogar_1.6.0.txt').read_bytes()+b'\n'+(REPO/'development/1.7.3/Migracion_Produccion.gs').read_bytes()
with tempfile.TemporaryDirectory(prefix='hogar-public-url-') as folder:
 script=Path(folder)/'script.txt';script.write_bytes(builder.patch(base));env=dict(os.environ,HOGAR_CONNECTION_SCRIPT=str(script))
 subprocess.run(['node',str(ROOT/'tests/connection.test.cjs')],check=True,env=env)
