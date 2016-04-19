package mx.krieger.hackeourbano.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import mx.krieger.hackeourbano.R;
import mx.krieger.internal.commons.androidutils.fragment.NavDrawerFragment;

public class InfoFragment extends NavDrawerFragment {
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.frag_info_toolbar);

        String version = "?";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
        }
        String s = "<b>'Ruta de 10' Versión: "+version+"</b><br><br>'Ruta de 10' obtiene las rutas de transporte concesionado más cercanas a la ubicación del usuario y permite ver información detallada sobre ellas, así como calificarlas y proporcionar retroalimentación. Esta aplicación fue creada dentro de #HackeoUrbano y es alimentada mediante la API de la base de datos del transporte público concesionado de la Ciudad de México generada en Mapatón CDMX.";
        s += "<br><br><b>Enlaces<b><br><br>";
        s += "<a href=\"http://mapatoncd.mx/\">MapatonCDMX</a><br><br>";
        s += "<a href=\"http://www.pidesinnovacion.org/hack/\">#HackeoUrbano</a><br><br>";
        s += "<a href=\"http://www.pidesinnovacion.org/\">PIDES Innovación Social</a><br><br>";
        s += "<a href=\"http://krieger.mx/\">Krieger</a><br><br>";
        ((TextView)v.findViewById(R.id.frag_info_tv)).setText(Html.fromHtml(s));
        return v;
    }

    @Override
    protected ToolbarConfiguration onToolbarConfiguration() {
        ToolbarConfiguration config = new ToolbarConfiguration();
        config.toolbar = toolbar;
        config.screenTitle = getString(R.string.section_name_info);
        config.drawerOpenStringID = R.string.drawer_open;
        config.drawerCloseStringID = R.string.drawer_close;
        return config;
    }
}
